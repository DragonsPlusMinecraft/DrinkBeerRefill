# 酒水流体与 Create/JEI 兼容审计

审计日期：2026-08-15
目标版本：Minecraft 1.21.1、NeoForge 21.1.244、Fabric API 0.116.12、Create 6.0.10、JEI 19.21.0.246

## 结论

DrinkBeer Refill 现在为 9 种基础酒提供标准流体和物品容器接口，每杯严格为 250 mB。实现不注册流体方块或桶，不改变混合酒和啤酒桶机制，也不把 Create 或 JEI 变成必需依赖。

NeoForge 额外包含两个彼此独立的客户端兼容点：一个可选 Mixin 修正 Create 的 JEI Spout 展示数量；一个条件式内置资源包把已经安装的 Create 牛奶精灵加入方块图集。两者都在条件不满足或上游发生变化时安全失效，不影响游戏启动和实际机器逻辑。Fabric 始终使用原版水纹理加酒水色值。

## 参考实现审计

- [Brewin' and Chewin'](https://modrinth.com/mod/brewin-and-chewin) 4.4.2 的发布包采用标准流体能力、着色和动态兼容思路。没有发现需要为每种饮料复制一份 Create 罐装配方的理由。
- Create 6.0.x 的 [`SpoutCategory`](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/compat/jei/category/SpoutCategory.java) 用 1000 mB 流体探测可填充物品，却没有把 `IFluidHandlerItem.fill` 返回的实际填充量写回展示栈。因此酒杯实际只接收 250 mB，但原始 JEI 页面显示 1000 mB。
- Create 的 [`CreateJEI`](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/compat/jei/CreateJEI.java) 会从物品流体能力动态生成 Spout 条目。再添加 `create:filling` JSON 会与动态条目重复，所以本模组没有增加显式罐装配方。
- Create 的牛奶 PNG 位于 `assets/create/textures/fluid/milk_still.png` 和 `milk_flow.png`，但这两个精灵并不天然属于 Minecraft 方块图集。仅把资源 ID 交给流体渲染器会产生缺失纹理；必须额外声明图集 source。

## 已实现范围

### 基础流体

- 注册 9 对 source/flowing 流体，共 18 个稳定注册表 ID。
- 一杯在 Fabric 中为 20,250 droplets，在 NeoForge 中为 250 mB，均等于四分之一桶。
- 成品酒杯可完整排空为对应流体和空杯；空杯可由对应流体完整填充为成品酒杯。
- NeoForge 容器拒绝部分处理、错误流体和堆叠容器，模拟操作不改物品，并保留物品数据组件。
- 每种酒具有独立流体标签，`#drinkbeer:beers` 聚合全部 9 种酒的 source/flowing 变体。
- 流体没有方块和桶，不能放置到世界中。

### Create/JEI 展示修正

- `drinkbeer.create_jei.mixins.json` 为 `required:false`、客户端配置，注入点 `require=0`。
- 配置插件仅在加载列表同时包含 `create` 和 `jei` 时启用目标 Mixin。
- 重定向范围仅为 `SpoutCategory.consumeRecipes` 对 `IFluidHandlerItem.fill` 的调用。
- 原始 `fill` 始终先执行；仅当返回值大于零且流体属于 DrinkBeer 的 9 种流体时，才把 JEI 展示栈数量改成返回值。
- 返回值、动作类型、容器结果、其他模组流体和运行时 Spout 逻辑均不改变。
- 没有 Create 编译依赖，也没有新增 `create:filling` 配方。

### 条件式乳状纹理

- 只有烈焰牛奶世涛与泡沫粉红蛋奶酒标记为 `MILKY`。
- NeoForge 同时确认 Create 已加载并且两张牛奶 PNG 都实际存在后，才启用 Create 纹理。
- 内置包 `resourcepacks/create_milk_atlas` 只有 `pack.mcmeta` 和 `assets/minecraft/atlases/blocks.json`；图集声明包含两个 `single` source，不包含、复制或修改任何 Create/CCK 图片或动画元数据。
- 同一检测结果同时控制资源包注册和渲染器纹理选择。任一文件缺失时不注册资源包，并回退原版水纹理，因此不会出现紫黑缺失纹理。
- Fabric 的代码和资源不引用 Create 牛奶纹理，9 种流体统一使用水纹理与各自 ARGB 色值。

## 验证结果

- Java 21 完整执行 `gradlew build`：通过。
- Fabric GameTest：5/5 通过；NeoForge GameTest：5/5 通过。
- 注册表快照覆盖全部 18 个流体 ID；公共测试覆盖配对、物品映射、标签、无桶语义和混合酒排除。
- Fabric 与 NeoForge 容器往返测试通过；NeoForge 额外覆盖模拟、错误流体、部分容量、堆叠酒杯和数据组件保留。
- JEI 修正测试覆盖 DrinkBeer 1000→250 mB、非本模组流体、填充失败、动作/返回值/容器透传。
- 图集测试确认内置包恰好只有两份声明文件、恰好引用两个外部精灵；缺任一外部文件时检测失败；Fabric 静态检查无 Create 牛奶资源引用。
- NeoForge Create 6.0.10 + JEI 实机中，空杯的 Spout 分类恰好有 9 页且无重复；两种乳状酒均显示 250 mB，并使用带本模组色值的 Create 牛奶动画。
- 实机执行资源重载后，条件式内置包再次加载，两种乳状酒仍正确显示；日志没有 Create 牛奶精灵缺失或加载错误。
- NeoForge 客户端启动矩阵的四种组合（无 Create/JEI、仅 JEI、仅 Create、Create+JEI）均成功到达标题界面；仅 Create 时条件式牛奶图集正常启用，其余不满足条件的组合不会注册该图集。
- NeoForge 专用 GameTest 服务器在没有 Create、存在 JEI 运行时的情况下成功启动并结束，证明可选 Mixin 不会污染服务端加载。

## 兼容边界与残余风险

- 混合酒携带基础酒和香料数据，固定流体无法无损表达，仍保持 item-only。
- 啤酒桶继续产出酒杯，不改成内部流体储罐。
- 未添加未经生态确认的 `c:beers` 等公共标签。
- Create 若移动或删除牛奶资源，乳状酒自动改用水纹理；不会阻止客户端启动。
- Create 若修改 `SpoutCategory.consumeRecipes` 的字节码结构，可选 Mixin 会跳过注入。最坏情况是 JEI 再次显示 1000 mB，实际 250 mB 容器和机器行为不受影响。
- JEI 修正以 Create 6.0.x 为目标，并在 6.0.10 上完成实机验收；升级 Create 后应重新检查目标方法和 JEI 页面。
- Fabric 暂不加入外部牛奶精灵图集兼容，因此乳状酒在 Fabric 上使用水动画。

Create、Flywheel、Ponder 和 Registrate 仅用于本次本地实机验收，不属于构建依赖或交付内容。
