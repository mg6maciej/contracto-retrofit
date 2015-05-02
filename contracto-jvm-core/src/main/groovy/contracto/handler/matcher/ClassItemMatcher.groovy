package contracto.handler.matcher

import contracto.model.contract.Item
import contracto.model.reflect.ContractoClassType
import groovy.transform.CompileStatic

import java.lang.annotation.Annotation
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType

@CompileStatic
abstract class ClassItemMatcher {

    boolean failOnNotImplementedFields = true

    abstract protected boolean withBody(Annotation[] annotations)

    boolean checkClassMatchItem(ContractoClassType classType, Item item) {
        if (item.type.simple) {
            return checkSimpleTypeMatch(classType, item)
        } else if (item.type.object) {
            return checkObjectTypeMatch(classType, item)
        } else if (item.type.array) {
            return checkArrayTypeMatch(classType, item)
        } else {
            throw new IllegalArgumentException("You shall not pass!")
        }
    }

    private boolean checkObjectTypeMatch(ContractoClassType classType, Item item) {
        if (failOnNotImplementedFields) {
            if (!missingFields(classType, item).isEmpty())
                return false
        }
        return existingFields(classType, item).every {
            checkClassMatchItem(classType.findDeclaredField(it.name, classTypeResolver), it)
        }
    }

    abstract protected ContractoClassType.ToClass getClassTypeResolver()

    private List<Item> existingFields(ContractoClassType classType, Item item) {
        return item.embedded.findAll { existsInClass(classType, it) }
    }

    private List<Item> missingFields(ContractoClassType classType, Item item) {
        return item.embedded.findAll { !existsInClass(classType, it) }
    }

    private boolean existsInClass(ContractoClassType classType, Item item) {
        return classType.findDeclaredField(item.name, classTypeResolver) != null
    }

    private boolean checkSimpleTypeMatch(ContractoClassType classType, Item item) {
        return item.type.possibleClasses.any { it.isAssignableFrom((Class) classType.type) }
    }

    private boolean checkArrayTypeMatch(ContractoClassType classType, Item item) {
        if (classType.type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) classType.type
            if (List.isAssignableFrom((Class) parameterizedType.rawType)) {
                def cct = new ContractoClassType(type: parameterizedType.actualTypeArguments[0])
                return checkClassMatchItem(cct, item.embedded.first())
            }
        } else if (classType.type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) classType.type
            def cct = new ContractoClassType(type: genericArrayType.genericComponentType)
            return checkClassMatchItem(cct, item.embedded.first())
        }
        return false
    }
}