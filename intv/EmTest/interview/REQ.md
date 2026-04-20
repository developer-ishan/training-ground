llm gateway

routes traffic

- claude proviers-def
- ai - secondary - fallback


split based on health and act as fallback

rejection policies

recovery and health checks

100 c, 0 oai
5 c, 95 oai
5 c, 5 oai r 90%



x(failure), y(recovery)

//
LlmGateWay
- Rules
[x, y configurations]
// satify
- Strategies implements the AI strategy
[clude, openAI, etc]
- Tracker
tracks the incoming requests to the gateway

askPrompt(){

    //log the incoming request

    // load rules as per last history

    //provide the strategy as per the rules

    //offload to each provider
    claude.askPrompt()

    //log the success or failure in tracker async to free up thread
}



x = 5 //last 5 requests failed with claude offload 5%
y = 3 //last 3 requests successed claude is up
      

x.c = 5 (%) 95
x.oai = 5 (5 5, 90%)

x.c, x.oai, y.c, y.oai

