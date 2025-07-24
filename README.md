# ResearchTreeMod

ResearchTreeMod - The mod adds a research tree to the game, which will be useful for developers who want to make a step-by-step development.

## Integration
- CraftTweaker
- KubeJS

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

| Method                 | Params                         | Return          | Descriptions                                                                                                               | Example                                                                                                        |
|------------------------|--------------------------------|-----------------|----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| addParent              | researchId as ResourceLocation | ResearchBuilder | Adds N to the study as a parent                                                                                            | addParent(<resource:minecraft:research_1>);                                                                    |
| addRequirement         | id as String, arg as Object[]  | ResearchBuilder | Adds requirements to the research. Accepts the requirement ID and arguments                                                | addRequirement("net.sixik.researchtree.research.requirements.ItemRequirements", <item:minecraft:diamond> * 42) |
| addReward              | id as String, arg as Object[]  | ResearchBuilder | Adds reward to the research. Accepts the reward ID and arguments                                                           | addReward("net.sixik.researchtree.research.rewards.ItemReward", <item:minecraft:iron_ingot> * 5)               |
| addDescription         | text as String                 | ResearchBuilder | Adds a description                                                                                                         | addDescription("Hello World!);                                                                                 |
| addIcon                | iconPath as ResourceLocation   | ResearchBuilder | Adds an icon                                                                                                               |                                                                                                                |
| stopping               | value as bool                  | ResearchBuilder | Can the research be stopped after the start?                                                                               | stopping(true);                                                                                                |
| researchTime           | time as long                   | ResearchBuilder | The time in milliseconds required for research. The default value is -1. If -1 then the time will be taken from the config | researchTime(50_000);                                                                                          |
| shouldRenderConnection | value as bool                  | ResearchBuilder | Whether to render display dependence on parents                                                                            | shouldRenderConnection(false);                                                                                 |
| refundPercent          | percent as double              | ResearchBuilder | How many resources will be returned if the study is canceled. By default, it takes the value from the config               | refundPercent(57.5d);                                                                                          |


## ContentIds

The ID of the elements to add to the study using `addRequirement` and `addReward`

### Requirements
| Id               | Arguments | Description                         |
|------------------|-----------|-------------------------------------|
| item_requirement | ItemStack | Requirements in the form of an item |

### Rewards
| Id          | Arguments | Description                   |
|-------------|-----------|-------------------------------|
| item_reward | ItemStack | Reward in the form of an item |

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