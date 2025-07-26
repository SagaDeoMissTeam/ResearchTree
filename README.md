# ResearchTreeMod

ResearchTreeMod - The mod adds a research tree to the game, which will be useful for developers who want to make a step-by-step development.

## Features
- Async Logic
- Automatic tree generation based on builder parameters
- Powerful Script API

## Integration
- CraftTweaker
- KubeJS
- FTB Teams
- FTB Quests

# For Modpack Developers
To create your own research trees, you can use scripts for 2 mods. (CraftTweaker, KubeJS) </br>

- *There is also a game editor inside.* **[Coming Soon]**

After creating the research tree, localization keys are generated for the title and for the subtitle based on the ID
- Title: `research.'namespace'.'path'`             `research.researchtreemod.myresearch_1`
- Subtitle: `research.'namespace'.'path'.subtitle` `research.researchtreemod.myresearch_1.subtitle`

## ScriptApi

### ResearchTreeBuilder
 
- For CraftTweaker ```import mods.researchtree.ResearchTreeBuilder;```

| Method      | Params                         | Return              | Descriptions                                                           |
|-------------|--------------------------------|---------------------|------------------------------------------------------------------------|
| create      | treeId as ResourceLocation     | ResearchTreeBuilder | Creates a research tree builder                                        |
| addResearch | researchId as ResourceLocation | ResearchBuilder     | Creates a research builder                                             |
| build       |                                |                     | The method that indicates the end of the creation of the research tree |

### ResearchBuilder

- For CraftTweaker ```import mods.researchtree.ResearchBuilder;```

| Method                   | Params                                                                  | Return             | Descriptions                                                                                                               | Example                                                                                                                                     |
|--------------------------|-------------------------------------------------------------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| addParent                | researchId as ResourceLocation                                          |                    | Adds N to the study as a parent                                                                                            | addParent(`<resource:minecraft:research_1>`);                                                                                               |
| addTrigger               | id as String, arg as Object[]                                           |                    | Adds triggers. Until they are completed, the research will be hidden.                                                      | addTrigger("break_block_trigger", `<block:minecraft:dirt>`);                                                                                |
| addCustomTrigger         | function as BiFunction<ServerPlayer, BaseResearch, Boolean>             |                    | Adds custom trigger. Until they are completed, the research will be hidden.                                                | addCustomTrigger((player, research) -> { player.sendSystemMessage("Complete!"); return true; } );                                           |
| addTriggerBuilder        | id as String, arg as Object[]                                           | TriggerBuilder     | Adds triggers. Until they are completed, the research will be hidden.                                                      | addTriggerBuilder("break_block_trigger", `<block:minecraft:dirt>`);                                                                         |
| addCustomTriggerBuilder  | function as BiFunction<ServerPlayer, BaseResearch, Boolean>             | TriggerBuilder     | Adds custom trigger. Until they are completed, the research will be hidden.                                                | addCustomTriggerBuilder((player, research) -> { player.sendSystemMessage("Complete!"); return true; } );                                    |
| addRequirement           | id as String, arg as Object[]                                           |                    | Adds requirements to the research. Accepts the requirement ID and arguments                                                | addRequirement("net.sixik.researchtree.research.requirements.ItemRequirements", `<item:minecraft:diamond>` * 42)                            |
| addReward                | id as String, arg as Object[]                                           |                    | Adds reward to the research. Accepts the reward ID and arguments                                                           | addReward("net.sixik.researchtree.research.rewards.ItemReward", `<item:minecraft:iron_ingot>` * 5)                                          |
| addRequirementBuilder    | id as String, arg as Object[]                                           | RequirementBuilder | Adds requirements to the research. Accepts the requirement ID and arguments                                                | addRequirement("net.sixik.researchtree.research.requirements.ItemRequirements", `<item:minecraft:diamond>` * 42).addTooltip("Hello World"); |
| addRewardBuilder         | id as String, arg as Object[]                                           | RewardBuilder      | Adds reward to the research. Accepts the reward ID and arguments                                                           | addReward("net.sixik.researchtree.research.rewards.ItemReward", `<item:minecraft:iron_ingot>` * 5).addTooltip("Hello World");               |
| addDescription           | text as String                                                          |                    | Adds a description                                                                                                         | addDescription("Hello World!);                                                                                                              |
| addIcon                  | iconType as String, arg as Object                                       |                    | Adds an icon                                                                                                               | addIcon("item", `<item:minecraft:iron_ingot>`);                                                                                             |
| stopping                 | value as bool                                                           |                    | Can the research be stopped after the start?                                                                               | stopping(true);                                                                                                                             |
| researchTime             | time as long                                                            |                    | The time in milliseconds required for research. The default value is -1. If -1 then the time will be taken from the config | researchTime(50_000);                                                                                                                       |
| shouldRenderConnection   | value as bool                                                           |                    | Whether to render display dependence on parents                                                                            | shouldRenderConnection(false);                                                                                                              |
| refundPercent            | percent as double                                                       |                    | How many resources will be returned if the study is canceled. By default, it takes the value from the config               | refundPercent(57.5d);                                                                                                                       |
| showType                 | index as ResearchShowType                                               |                    | Research Rendering Conditions                                                                                              | showType(0);                                                                                                                                |
| hideTypeRender           | index as ResearchHideTypeRender                                         |                    | The type of render when the ResearchShowType condition is met                                                              | hideTypeRender(0);                                                                                                                          |
| addFunctionOnStart       | executeStage as int, functionId as String, args as Object[]             |                    | Adds a function that will be performed at the beginning of the study                                                       | addFunctionOnStart(1,"command", "/time set day");                                                                                           |
| addFunctionOnEnd         | executeStage as int, functionId as String, args as Object[]             |                    | Adds a function that will be performed at the end of the study                                                             | addFunctionOnEnd(0,"command", "/time set day");                                                                                             |
| addCustomFunctionOnStart | executeStage as int, function as BiConsumer<ServerPlayer, BaseResearch> |                    | Adds a custom function that will be performed at the start of the study                                                    | addCustomFunctionOnStart(1, (player, research) -> { player.sendSystemMessage("Hello world!"); })                                            |
| addCustomFunctionOnEnd   | executeStage as int, function as BiConsumer<ServerPlayer, BaseResearch> |                    | Adds a custom function that will be performed at the end of the study                                                      | addCustomFunctionOnEnd(1, (player, research) -> { player.sendSystemMessage("Hello world!"); })                                              |

### RequirementBuilder or RewardBuilder
- For CraftTweaker ```import mods.researchtree.RewardBuilder;```
- For CraftTweaker ```import mods.researchtree.RequirementBuilder;```

| Method     | Params              | Return          | Description            | Example                               |
|------------|---------------------|-----------------|------------------------|---------------------------------------|
| addTooltip | tooltip as String[] | Object          | Add tooltip for object | addTooltip("my.key.for.localization") |
| end        |                     | ResearchBuilder |                        | end()                                 |

### TriggerBuilder
- For CraftTweaker ```import mods.researchtree.TriggerBuilder;```

| Method            | Params                                             | Return          | Description                            | Example                                                                                   |
|-------------------|----------------------------------------------------|-----------------|----------------------------------------|-------------------------------------------------------------------------------------------|
| addFunction       | functionId as String, args as Object[]             | TriggerBuilder  | Executed after the trigger is complete | addFunction("command", "/time set day");                                                  |
| addCustomFunction | function as BiConsumer<ServerPlayer, BaseResearch> | TriggerBuilder  | Executed after the trigger is complete | addCustomFunction(1, (player, research) -> { player.sendSystemMessage("Hello world!"); }) |
| end               |                                                    | ResearchBuilder |                                        | end()                                                                                     |


## ContentIds

The ID of the elements to add to the study using `addRequirement` and `addReward`

### Requirements
| Id                    | Arguments | Description                          |
|-----------------------|-----------|--------------------------------------|
| item_requirement      | ItemStack | Requirements in the form of an item  |
| stage_requirement     | String    | Requirements in the form of an stage |
| ftbquests_requirement | Long      | Requirements in the form of an quest |

### Rewards
| Id               | Arguments                        | Description                                                             |
|------------------|----------------------------------|-------------------------------------------------------------------------|
| item_reward      | ItemStack                        | Reward in the form of an item                                           |
| command_reward   | String                           | Reward in the form of an command. Support {player}                      |
| stage_reward     | String                           | Reward in the form of an stage                                          |
| structure_reward | ResourceLocation, offsetY as int | As a reward, it creates a structure based on the player's coordinates.  |
| ftbquests_reward | Long                             | Reward in the form of an quest                                          |

### Triggers

| Id                  | Arguments                                                         | Description                                                       | Example                                                                                                                                                                            |
|---------------------|-------------------------------------------------------------------|-------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| block_break_trigger | Block or BlockState                                               | Executed when the block was broken                                |                                                                                                                                                                                    |
| item_trigger        | ItemStack                                                         | It is performed when the player has an object in one of his hands |                                                                                                                                                                                    |
| locate_trigger      | LocateType(Id), LocateType(Arguments)                             | It is executed when the player is in the specified location.      |                                                                                                                                                                                    |
| entity_kill_trigger | entityId as ResourceLocation                                      | Executed when the player kills a mob                              |                                                                                                                                                                                    |
| player_stat_trigger | id as ResourceLocation, value as int, object as Object (Optional) | Executed when the player has N stats values.                      | addTrigger("player_stat_trigger", `<resource:minecraft:leave_game>`, 20); </br> addTrigger("player_stat_trigger", `<resource:minecraft:broken>`, 20, `<item:minecraft:iron_axe>`); |
| ftbquests_trigger   | questsId as long                                                  | Execute when quests complete                                      |                                                                                                                                                                                    |

### Functions

| Id      | Arguments         | Description                                                                                                                     |
|---------|-------------------|---------------------------------------------------------------------------------------------------------------------------------|
| command | command as String | Execute the specified command. Support {player}. If you need to specify a player, use {player} `"/say {player} hello player !"` |

### FunctionStage

| index | Description                 |
|-------|-----------------------------|
| 0     | Runs before the main logic. |
| 1     | Runs after the main logic.  |


### ResearchHideTypeRender
Responsible for how the rendering will take place if the research is hidden

| index | Description                                                                            |
|-------|----------------------------------------------------------------------------------------|
| 0     | Renders the question mark                                                              |
| 1     | Renders only the outline of the object/icon.                                           |
| 2     | Completely hidden. Not visible until the conditions are met                            |
| 3     | The study has an icon and text visible, but it will be slightly dim and not clickable. |

### ResearchShowType
Responsible for the display conditions

| index | Description                                                 |
|-------|-------------------------------------------------------------|
| 0     | Always Show                                                 |
| 1     | Hides it until at least one parent study is opened.         |
| 2     | Hides it until all the parents are open.                    |
| 3     | Hides it until any triggers are executed                    |
| 4     | Hides it until all triggers are executed                    |
| 5     | Hides it until custom trigger are executed                  |
| 6     | Always hidden until all conditions and dependencies are met |

### IconType

| Id      | Arguments                        | Description                                    |
|---------|----------------------------------|------------------------------------------------|
| texture | id as String or ResourceLocation | Sets the icon based on the path to the texture |
| item    | item as Item or ItemStack        | Sets the icon based on the item                |

### LocateType

| Id | Arguments        | Description |
|----|------------------|-------------|
| 0  | ResourceLocation | Dimension   |
| 1  | ResourceLocation | Biome       |
| 2  | ResourceLocation | Structure   |
| 3  | BlockPos         | Position    |

## Example

### CraftTweaker
```ts
import mods.researchtree.ResearchTreeBuilder;

var builder = ResearchTreeBuilder.create(<resource:minecraft:test>);

builder.addResearch(<resource:minecraft:test_1>).addRequirement("item_requirement", <item:minecraft:diamond> * 2).addParent(<resource:minecraft:test2>)
.stopping(false)
.addDescription("Hello World!");

builder.addResearch(<resource:minecraft:test2>).addRequirement("item_requirement", <item:minecraft:diamond>)
.addRequirement("item_requirement", <item:minecraft:iron_ingot> * 5).addReward("item_reward", <item:minecraft:bedrock> * 6)
.refundPercent(57.5D);

builder.build();
```

### KubeJS 
```js
let builder = ResearchTreeBuilder.create('minecraft:test');

builder.addResearch('minecraft:test_1').addRequirement("item_requirement", Item.of('minecraft:diamond'))
.addParent('minecraft:test2')
.stopping(false)
.addDescription("Hello World!");

builder.addResearch('minecraft:test2').addRequirement("item_requirement", Item.of('minecraft:diamond'))
.addRequirement("item_requirement", Item.of('minecraft:iron_ingot')).addReward("item_reward", Item.of('2x minecraft:bedrock'))
.refundPercent(57.5);

builder.build();
```