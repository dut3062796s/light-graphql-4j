package com.networknt.graphql.router;

import com.networknt.config.Config;
import com.networknt.graphql.common.GraphqlUtil;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ServiceLoader;


/**
 * Created by steve on 24/03/17.
 */
public class GraphqlPostHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(GraphqlPostHandler.class);
    static GraphQLSchema schema = null;
    static {
        // load GraphQL Schema with service loader. It should be defined in SchemaProvider
        final ServiceLoader<SchemaProvider> schemaLoaders = ServiceLoader.load(SchemaProvider.class);
        for (final SchemaProvider provider : schemaLoaders) {
            if (provider.getSchema() != null) {
                schema = provider.getSchema();
                break;
            }
        }
        if (schema == null) {
            logger.error("Unable to load GraphQL schema - no SchemaProvider implementation available in the classpath");
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // get the request parameters as a Map<String, Object>
        Map<String, Object> requestParameters = (Map<String, Object>)exchange.getAttachment(GraphqlUtil.GRAPHQL_PARAMS);
        if(logger.isDebugEnabled()) logger.debug("requestParameters: " + requestParameters);

        GraphQL graphQL = new GraphQL(schema);
        Object result = graphQL.execute((String)requestParameters.get("query")).getData();
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}