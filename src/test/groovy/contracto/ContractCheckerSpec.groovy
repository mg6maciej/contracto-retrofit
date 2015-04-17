package contracto

import client.data.MyData
import spock.lang.Specification

import static contracto.ContractChecker.checkClassMatchItem
import static contracto.Type.*

class ContractCheckerSpec extends Specification {

    def "Should return true when object match body"() {
        expect:
        checkClassMatchItem(MyData, newItem(object, 'id', string))
    }

    def "Should return false when object doesn't match body"() {
        expect:
        !checkClassMatchItem(MyData, newItem(type, embeddedItemName, embeddedItemType))
        where:
        type   | embeddedItemName | embeddedItemType
        object | 'name'           | string
        object | 'id'             | number
    }

    private static Item newItem(type, String embeddedItemName, embeddedItemType) {
        return new Item(
                type: type,
                embedded: [
                        new Item(
                                name: embeddedItemName,
                                type: embeddedItemType,
                        ),
                ],
        )
    }
}
