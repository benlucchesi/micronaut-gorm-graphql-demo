
package com.accuracysoftawre

import org.grails.datastore.gorm.GormEntity
import java.util.Collection
import org.grails.datastore.mapping.model.types.*

import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager


class SimpleGormEntityBinder implements GraphQLDataBinder {
 
  MappingContext mappingContext
  GraphQLDataBinderManager dataBinderManager

  SimpleGormEntityBinder(MappingContext mappingContext, GraphQLDataBinderManager dataBinderManager){
    this.mappingContext = mappingContext
    this.dataBinderManager = dataBinderManager
  }

  @Override
  void bind( Object entity, Map data ){
    bindEntity(entity,data)
    println "Entity Errors: ${entity.errors.allErrors}"
  }

  void bindEntity( Object entity, Map data ){
   
    assert entity != null 
    assert data != null 

    def persistentEntity = mappingContext.getPersistentEntity(entity.class.name)
    if( persistentEntity == null ){
      throw new RuntimeException("PersistentEntity not mapped to ${entity.class.name}. ")
    }

    entity.properties.each{ entityProperty ->   

      def persistentProperty = persistentEntity.getPropertyByName(entityProperty.key)
      if( persistentProperty == null || data.containsKey(entityProperty.key) == false )
        return;

      switch( persistentProperty ){

        case {persistentProperty instanceof Simple}:
          entity."${entityProperty.key}" = data[entityProperty.key]
          break;

        case {persistentProperty instanceof ManyToOne}:
          def associatedEntity = createOrGetEntityAndBindData( persistentProperty.associatedEntity, data[entityProperty.key] )
          entity."${entityProperty.key}" = associatedEntity
          if( persistentProperty.referencedPropertyName )
            associatedEntity.addTo( "${persistentProperty.referencedPropertyName}", entity )
          break
        
        case {persistentProperty instanceof OneToOne}:
          def associatedEntity = createOrGetEntityAndBindData( persistentProperty.associatedEntity, data[entityProperty.key] )
          if( persistentProperty.referencedPropertyName )
            associatedEntity."${persistentProperty.referencedPropertyName}" = entity
          entity."${entityProperty.key}" = associatedEntity
          break

        case {persistentProperty instanceof Basic}:
        case {persistentProperty instanceof OneToMany}:
        case {persistentProperty instanceof ManyToMany}:
          bindEntityCollection( entity, persistentProperty, data[entityProperty.key] )
          break

        case {persistentProperty instanceof Embedded}:
          def associatedEntity = createOrGetEntityAndBindData( persistentProperty.associatedEntity, data[entityProperty.key] )
          entity."${entityProperty.key}" = associatedEntity
          break

        case {persistentProperty instanceof EmbeddedCollection}:
          bindEntityCollection( entity, persistentProperty, data[entityProperty.key] )
          break
      }
        
    }

  }

  void bindEntityCollection( Object owningEntity, PersistentProperty property, Collection items ){
   
    def entities = items.collect{ item ->
      def entity = createOrGetEntityAndBindData( property.associatedEntity, item )
    }

    if( property.isBidirectional() ){
      if( property.isOwningSide() ){
        entities.each{  
          println "1 - adding ${it} to ${owningEntity}" 
          owningEntity.addTo(property.name, it ) 
        }
      }
      else{
        entities.each{ 
          println "2 - adding ${owningEntity} to ${it}" 
          it.addTo(property.referencedPropertyName, owningEntity) 
        }
      }
    }
    else{
      entities.each{  
        if( it instanceof GormEntity ){
          it.save()
        }
        owningEntity.addTo(property.name, it ) 
      }
    }
  }


  Object createOrGetEntityAndBindData( PersistentEntity itemType, Object data ){
    // if the data is a map
    def entity = null

    if( data instanceof Map ){

      if( ((Map)data).containsKey('id') )
        entity = itemType.getJavaClass().get(data['id'])
      else
        entity = itemType.newInstance() 

      def binder = dataBinderManager.getDataBinder(entity.class) ?: this
      binder.bind( entity, (Map)data )
    }
    else{
      entity = data
    }

    return entity
  }

  // List collectErrors(GormEntity entity, PersistentProperty persistentProperty){
  //  
  //   List errors = []

  //   if( entity.hasErrors() ){
  //     entity.errors.each{
  //       errors.push( it )
  //     }
  //   }
  //    
  //   switch( persistentProperty ){

  //     case {persistentProperty instanceof Simple}:
  //       break;

  //     case {persistentProperty instanceof Embedded}:
  //     case {persistentProperty instanceof OneToOne}:
  //     case {persistentProperty instanceof ManyToOne}:
  //       errors.addAll( collectErrors(it, persistentProperty)  )
  //       break
  //     
  //     case {persistentProperty instanceof Basic}:
  //       // no error detecting needed
  //       break;

  //     case {persistentProperty instanceof OneToMany}:
  //     case {persistentProperty instanceof ManyToMany}:
  //       associatedEntity.each{ errors.addAll( collectErrors(associatedEntity, persistentProperty)  ) } 
  //       break

  //     case {persistentProperty instanceof EmbeddedCollection}:
  //       // ?????
  //       break
  //   }
 
  //   return errors 
  // }

}
