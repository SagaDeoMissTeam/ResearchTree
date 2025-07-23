
# For Modpack Developers
To create your own research trees, you can use scripts for 2 mods. </br>

- *There is also a game editor inside.* **[Coming Soon]**

## Crafttweaker

```ts
import mods.researchtree.ResearchTreeBuilder;

ResearchTreeBuilder.create()
```

### ResearchTreeBuilder

```import mods.researchtree.ResearchTreeBuilder;```

| Method      | Params                         | Return              | Descriptions                                                           |
|-------------|--------------------------------|---------------------|------------------------------------------------------------------------|
| create      | treeId as ResourceLocation     | ResearchTreeBuilder | Creates a research tree builder                                        |
| addResearch | researchId as ResourceLocation | ResearchBuilder     | Creates a research builder                                             |
| build       |                                |                     | The method that indicates the end of the creation of the research tree |

### ResearchBuilder
```import mods.researchtree.ResearchBuilder;```

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


## KubeJS