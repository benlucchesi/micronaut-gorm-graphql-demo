/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.accuracysoftawre

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.Schema
import graphql.schema.GraphQLSchema

import org.grails.datastore.mapping.core.Datastore

import org.grails.gorm.graphql.interceptor.impl.BaseGraphQLFetcherInterceptor
import graphql.schema.DataFetchingEnvironment
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType

class AuthenticationCheck extends BaseGraphQLFetcherInterceptor {

  // boolean	onCustomMutation(java.lang.String name, DataFetchingEnvironment environment)
  // boolean	onCustomQuery(java.lang.String name, DataFetchingEnvironment environment)
  // boolean	onMutation(DataFetchingEnvironment environment, GraphQLDataFetcherType type)
  boolean	onQuery(DataFetchingEnvironment environment, GraphQLDataFetcherType type){
    // println environment
    // println type
    // throw new RuntimeException("access denied")
    return true
  }

}

@Factory
@SuppressWarnings("Duplicates")
class GraphQLFactory {

    @Bean
    @Singleton
    GraphQL graphQL(Datastore datastore) {

      def mappingContext = datastore.mappingContext
      Schema schema = new Schema( mappingContext )
      schema.initialize()
      def interceptorManager = schema.interceptorManager

      // configure security interceptors here....
      interceptorManager.registerInterceptor(Book, new AuthenticationCheck() )

      GraphQLSchema graphQLSchema = schema.generate()

      // Return the GraphQL bean.
      return GraphQL.newGraphQL(graphQLSchema).build();
    }
}



