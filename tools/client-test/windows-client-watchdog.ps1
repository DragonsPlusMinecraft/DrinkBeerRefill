[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('Start', 'Monitor', 'Await')]
    [string]$Action,

    [Parameter(Mandatory = $true)]
    [string]$EvidenceDirectory,

    [Parameter(Mandatory = $true)]
    [ValidateSet('fabric', 'neoforge')]
    [string]$Loader,

    [string]$ExpectedBackend = 'OpenGL',
    [int]$WindowTimeoutSeconds = 180,
    [int]$WorldTimeoutSeconds = 300,
    [int]$ExitTimeoutSeconds = 60
)

$ErrorActionPreference = 'Stop'
$evidencePath = [System.IO.Path]::GetFullPath($EvidenceDirectory)
$processMarkerPath = Join-Path $evidencePath 'process.json'
$phaseMarkerPath = Join-Path $evidencePath 'phase.json'
$successMarkerPath = Join-Path $evidencePath 'success.json'
$failureMarkerPath = Join-Path $evidencePath 'failure.json'
$watchdogResultPath = Join-Path $evidencePath 'watchdog.json'
$watchdogPidPath = Join-Path $evidencePath 'watchdog.pid'
$windowEvidencePath = Join-Path $evidencePath 'windows'

function Write-JsonAtomic {
    param(
        [Parameter(Mandatory = $true)] [string]$Path,
        [Parameter(Mandatory = $true)] [object]$Value
    )

    $temporaryPath = "$Path.tmp"
    $Value | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $temporaryPath -Encoding UTF8
    Move-Item -LiteralPath $temporaryPath -Destination $Path -Force
}

function Read-JsonRetry {
    param([Parameter(Mandatory = $true)] [string]$Path)

    for ($attempt = 0; $attempt -lt 10; $attempt++) {
        try {
            return Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
        }
        catch {
            Start-Sleep -Milliseconds 100
        }
    }
    throw "Unable to parse JSON marker $Path"
}

function Stop-ExactClientProcess {
    param([Nullable[int]]$ProcessId)

    if ($null -eq $ProcessId) {
        return
    }
    $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if ($null -ne $process) {
        Stop-Process -Id $ProcessId -Force
    }
}

function Fail-Watchdog {
    param(
        [Parameter(Mandatory = $true)] [string]$Message,
        [Nullable[int]]$ClientProcessId,
        [string]$WindowTitle = ''
    )

    Stop-ExactClientProcess -ProcessId $ClientProcessId
    Write-JsonAtomic -Path $watchdogResultPath -Value ([ordered]@{
            status = 'failure'
            loader = $Loader
            expectedBackend = $ExpectedBackend
            processId = $ClientProcessId
            windowTitle = $WindowTitle
            message = $Message
            finishedAt = [DateTimeOffset]::UtcNow.ToString('O')
        })
    throw $Message
}

if ($Action -eq 'Start') {
    New-Item -ItemType Directory -Path $evidencePath -Force | Out-Null
    Remove-Item -LiteralPath $watchdogResultPath -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $watchdogPidPath -Force -ErrorAction SilentlyContinue

    $powershell = Join-Path $PSHOME 'powershell.exe'
    $arguments = @(
        '-NoLogo',
        '-NoProfile',
        '-ExecutionPolicy', 'Bypass',
        '-File', ('"{0}"' -f $PSCommandPath),
        '-Action', 'Monitor',
        '-EvidenceDirectory', ('"{0}"' -f $evidencePath),
        '-Loader', $Loader,
        '-ExpectedBackend', $ExpectedBackend,
        '-WindowTimeoutSeconds', $WindowTimeoutSeconds,
        '-WorldTimeoutSeconds', $WorldTimeoutSeconds,
        '-ExitTimeoutSeconds', $ExitTimeoutSeconds
    )
    # Shell execution is intentional here: it prevents the monitor from
    # inheriting Gradle's redirected standard handles, which would otherwise
    # keep the short Start action alive until the entire client test finished.
    $watchdog = Start-Process -FilePath $powershell -ArgumentList $arguments -WindowStyle Hidden -PassThru
    Set-Content -LiteralPath $watchdogPidPath -Value $watchdog.Id -Encoding ASCII
    return
}

if ($Action -eq 'Await') {
    if (-not (Test-Path -LiteralPath $watchdogPidPath -PathType Leaf)) {
        throw "Missing watchdog PID marker $watchdogPidPath"
    }
    $watchdogPid = [int](Get-Content -LiteralPath $watchdogPidPath -Raw).Trim()
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($WorldTimeoutSeconds + $ExitTimeoutSeconds + 30)
    while ($null -ne (Get-Process -Id $watchdogPid -ErrorAction SilentlyContinue)) {
        if ([DateTimeOffset]::UtcNow -ge $deadline) {
            $clientPid = $null
            if (Test-Path -LiteralPath $processMarkerPath -PathType Leaf) {
                $clientPid = [int](Read-JsonRetry -Path $processMarkerPath).processId
            }
            Stop-ExactClientProcess -ProcessId $clientPid
            Stop-Process -Id $watchdogPid -Force -ErrorAction SilentlyContinue
            throw "Client watchdog $watchdogPid exceeded its own deadline"
        }
        Start-Sleep -Milliseconds 250
    }

    if (-not (Test-Path -LiteralPath $watchdogResultPath -PathType Leaf)) {
        throw 'Client watchdog exited without a result marker'
    }
    $result = Read-JsonRetry -Path $watchdogResultPath
    if ($result.status -ne 'success') {
        throw "Client watchdog failed: $($result.message)"
    }
    return
}

Add-Type -AssemblyName System.Drawing
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using System.Text;

public static class DrinkBeerWindowApi
{
    public delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    [StructLayout(LayoutKind.Sequential)]
    public struct RECT
    {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }

    [DllImport("user32.dll")]
    public static extern bool EnumWindows(EnumWindowsProc callback, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern bool IsWindowVisible(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);

    [DllImport("user32.dll", CharSet = CharSet.Unicode)]
    public static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int count);

    [DllImport("user32.dll", CharSet = CharSet.Unicode)]
    public static extern int GetWindowTextLength(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern IntPtr GetForegroundWindow();

    [DllImport("user32.dll")]
    public static extern bool ShowWindowAsync(IntPtr hWnd, int command);

    [DllImport("user32.dll")]
    public static extern bool BringWindowToTop(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern void SwitchToThisWindow(IntPtr hWnd, bool altTab);

    [DllImport("user32.dll")]
    public static extern bool PrintWindow(IntPtr hWnd, IntPtr deviceContext, uint flags);
}
'@

function Find-VisibleWindowForProcess {
    param([Parameter(Mandatory = $true)] [int]$ProcessId)

    $script:matchedWindow = [IntPtr]::Zero
    $callback = [DrinkBeerWindowApi+EnumWindowsProc]{
        param([IntPtr]$window, [IntPtr]$state)
        if (-not [DrinkBeerWindowApi]::IsWindowVisible($window)) {
            return $true
        }
        [uint32]$owner = 0
        [void][DrinkBeerWindowApi]::GetWindowThreadProcessId($window, [ref]$owner)
        if ($owner -eq [uint32]$ProcessId -and [DrinkBeerWindowApi]::GetWindowTextLength($window) -gt 0) {
            $script:matchedWindow = $window
            return $false
        }
        return $true
    }
    [void][DrinkBeerWindowApi]::EnumWindows($callback, [IntPtr]::Zero)
    return $script:matchedWindow
}

function Get-WindowTitle {
    param([Parameter(Mandatory = $true)] [IntPtr]$Window)

    $length = [DrinkBeerWindowApi]::GetWindowTextLength($Window)
    $builder = New-Object System.Text.StringBuilder ($length + 1)
    [void][DrinkBeerWindowApi]::GetWindowText($Window, $builder, $builder.Capacity)
    return $builder.ToString()
}

function Save-WindowScreenshot {
    param(
        [Parameter(Mandatory = $true)] [IntPtr]$Window,
        [Parameter(Mandatory = $true)] [string]$Name
    )

    $rect = New-Object DrinkBeerWindowApi+RECT
    if (-not [DrinkBeerWindowApi]::GetWindowRect($Window, [ref]$rect)) {
        throw "GetWindowRect failed for window $Window"
    }
    $width = $rect.Right - $rect.Left
    $height = $rect.Bottom - $rect.Top
    if ($width -lt 320 -or $height -lt 200) {
        throw "Minecraft window is unexpectedly small: ${width}x${height}"
    }

    New-Item -ItemType Directory -Path $windowEvidencePath -Force | Out-Null
    $bitmap = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $deviceContext = $graphics.GetHdc()
        try {
            $printed = [DrinkBeerWindowApi]::PrintWindow($Window, $deviceContext, 2)
        }
        finally {
            $graphics.ReleaseHdc($deviceContext)
        }

        if (-not $printed) {
            [void][DrinkBeerWindowApi]::ShowWindowAsync($Window, 9)
            [void][DrinkBeerWindowApi]::BringWindowToTop($Window)
            [DrinkBeerWindowApi]::SwitchToThisWindow($Window, $true)
            [void][DrinkBeerWindowApi]::SetForegroundWindow($Window)
            Start-Sleep -Milliseconds 250
            if ([DrinkBeerWindowApi]::GetForegroundWindow() -ne $Window) {
                throw "Unable to activate exact Minecraft window $Window for capture"
            }
            $graphics.CopyFromScreen($rect.Left, $rect.Top, 0, 0,
                (New-Object System.Drawing.Size $width, $height),
                [System.Drawing.CopyPixelOperation]::SourceCopy)
        }
        $target = Join-Path $windowEvidencePath $Name
        $bitmap.Save($target, [System.Drawing.Imaging.ImageFormat]::Png)
        return $target
    }
    finally {
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

$monitorStarted = [DateTimeOffset]::UtcNow
$clientPid = $null
$minecraftWindow = [IntPtr]::Zero
$windowTitle = ''
$captures = New-Object System.Collections.Generic.List[string]

try {
    New-Item -ItemType Directory -Path $evidencePath -Force | Out-Null
    $windowDeadline = $monitorStarted.AddSeconds($WindowTimeoutSeconds)
    while (-not (Test-Path -LiteralPath $processMarkerPath -PathType Leaf)) {
        if (Test-Path -LiteralPath $failureMarkerPath -PathType Leaf) {
            Fail-Watchdog -Message "Client probe failed before publishing its PID" -ClientProcessId $null
        }
        if ([DateTimeOffset]::UtcNow -ge $windowDeadline) {
            Fail-Watchdog -Message "Minecraft process marker did not appear within $WindowTimeoutSeconds seconds" -ClientProcessId $null
        }
        Start-Sleep -Milliseconds 100
    }

    $processMarker = Read-JsonRetry -Path $processMarkerPath
    $clientPid = [int]$processMarker.processId
    if ($processMarker.loader -ne $Loader) {
        Fail-Watchdog -Message "Expected loader $Loader in process marker, got $($processMarker.loader)" `
            -ClientProcessId $clientPid
    }

    while ($minecraftWindow -eq [IntPtr]::Zero) {
        if ($null -eq (Get-Process -Id $clientPid -ErrorAction SilentlyContinue)) {
            Fail-Watchdog -Message "Client PID $clientPid exited before a visible Minecraft window appeared" `
                -ClientProcessId $clientPid
        }
        $minecraftWindow = Find-VisibleWindowForProcess -ProcessId $clientPid
        if ($minecraftWindow -ne [IntPtr]::Zero) {
            break
        }
        if ([DateTimeOffset]::UtcNow -ge $windowDeadline) {
            Fail-Watchdog -Message "No visible window owned by client PID $clientPid appeared within $WindowTimeoutSeconds seconds" `
                -ClientProcessId $clientPid
        }
        Start-Sleep -Milliseconds 100
    }

    $windowTitle = Get-WindowTitle -Window $minecraftWindow
    $captures.Add((Save-WindowScreenshot -Window $minecraftWindow -Name 'window-created.png'))

    $worldDeadline = $monitorStarted.AddSeconds($WorldTimeoutSeconds)
    $capturedPhases = @{}
    while (-not (Test-Path -LiteralPath $successMarkerPath -PathType Leaf)) {
        if (Test-Path -LiteralPath $failureMarkerPath -PathType Leaf) {
            $failure = Read-JsonRetry -Path $failureMarkerPath
            Fail-Watchdog -Message "Client probe failed in stage $($failure.stage): $($failure.message)" `
                -ClientProcessId $clientPid -WindowTitle $windowTitle
        }
        if ($null -eq (Get-Process -Id $clientPid -ErrorAction SilentlyContinue)) {
            Fail-Watchdog -Message "Client PID $clientPid exited without a success marker" `
                -ClientProcessId $clientPid -WindowTitle $windowTitle
        }
        if (Test-Path -LiteralPath $phaseMarkerPath -PathType Leaf) {
            $phaseMarker = Read-JsonRetry -Path $phaseMarkerPath
            $phase = [string]$phaseMarker.phase
            if (-not $capturedPhases.ContainsKey($phase) -and
                    @('world_ready', 'world_stable', 'f3', 'gui', 'jei', 'saving') -contains $phase) {
                $minecraftWindow = Find-VisibleWindowForProcess -ProcessId $clientPid
                if ($minecraftWindow -eq [IntPtr]::Zero) {
                    Fail-Watchdog -Message "Minecraft window disappeared during phase $phase" `
                        -ClientProcessId $clientPid -WindowTitle $windowTitle
                }
                $captures.Add((Save-WindowScreenshot -Window $minecraftWindow -Name ("window-$phase.png")))
                $capturedPhases[$phase] = $true
            }
        }
        if ([DateTimeOffset]::UtcNow -ge $worldDeadline) {
            Fail-Watchdog -Message "Client did not enter, validate, save, and mark its world within $WorldTimeoutSeconds seconds" `
                -ClientProcessId $clientPid -WindowTitle $windowTitle
        }
        Start-Sleep -Milliseconds 100
    }

    $success = Read-JsonRetry -Path $successMarkerPath
    if ($success.status -ne 'success' -or [int]$success.processId -ne $clientPid -or $success.loader -ne $Loader) {
        Fail-Watchdog -Message 'Client success marker does not match the watched process and loader' `
            -ClientProcessId $clientPid -WindowTitle $windowTitle
    }
    if (([string]$success.graphicsBackend).IndexOf($ExpectedBackend, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
        Fail-Watchdog -Message "Expected backend $ExpectedBackend but probe reported $($success.graphicsBackend)" `
            -ClientProcessId $clientPid -WindowTitle $windowTitle
    }
    if ([long]$success.renderedFrames -lt 200 -or [double]$success.stableSeconds -lt 15.0) {
        Fail-Watchdog -Message 'Client did not sustain 200 rendered frames for 15 seconds' `
            -ClientProcessId $clientPid -WindowTitle $windowTitle
    }
    foreach ($property in @('worldScreenshot', 'f3Screenshot', 'guiScreenshot', 'jeiScreenshot')) {
        if (-not (Test-Path -LiteralPath ([string]$success.$property) -PathType Leaf)) {
            Fail-Watchdog -Message "Missing required probe screenshot $property" `
                -ClientProcessId $clientPid -WindowTitle $windowTitle
        }
    }
    if (-not $capturedPhases.ContainsKey('world_ready')) {
        Fail-Watchdog -Message 'External watchdog did not observe and capture the loaded world phase' `
            -ClientProcessId $clientPid -WindowTitle $windowTitle
    }

    $exitDeadline = [DateTimeOffset]::UtcNow.AddSeconds($ExitTimeoutSeconds)
    while ($null -ne (Get-Process -Id $clientPid -ErrorAction SilentlyContinue)) {
        if ([DateTimeOffset]::UtcNow -ge $exitDeadline) {
            Fail-Watchdog -Message "Successful client PID $clientPid did not exit within $ExitTimeoutSeconds seconds" `
                -ClientProcessId $clientPid -WindowTitle $windowTitle
        }
        Start-Sleep -Milliseconds 100
    }

    Write-JsonAtomic -Path $watchdogResultPath -Value ([ordered]@{
            status = 'success'
            loader = $Loader
            expectedBackend = $ExpectedBackend
            actualBackend = $success.graphicsBackend
            processId = $clientPid
            windowHandle = $minecraftWindow.ToInt64()
            windowTitle = $windowTitle
            windowAppearedSeconds = ([DateTimeOffset]::UtcNow - $monitorStarted).TotalSeconds
            renderedFrames = [long]$success.renderedFrames
            stableSeconds = [double]$success.stableSeconds
            captures = $captures
            finishedAt = [DateTimeOffset]::UtcNow.ToString('O')
        })
}
catch {
    if (-not (Test-Path -LiteralPath $watchdogResultPath -PathType Leaf)) {
        Stop-ExactClientProcess -ProcessId $clientPid
        Write-JsonAtomic -Path $watchdogResultPath -Value ([ordered]@{
                status = 'failure'
                loader = $Loader
                expectedBackend = $ExpectedBackend
                processId = $clientPid
                windowTitle = $windowTitle
                message = $_.Exception.Message
                finishedAt = [DateTimeOffset]::UtcNow.ToString('O')
            })
    }
    throw
}
