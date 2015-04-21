package contracto

import contracto.api.ContractoService
import contracto.discovery.ContractoMethodFinder
import contracto.handler.MatchResultHandler
import contracto.matcher.ContractMatcher
import contracto.model.MatchResult
import contracto.model.contract.Contract
import contracto.model.reflect.ContractoMethod
import groovy.transform.CompileStatic

@CompileStatic
class Contracto {
    private ContractoService service = new ContractoService()
    private ContractoMethodFinder methodExtractor = new ContractoMethodFinder()
    private ContractMatcher matcher = new ContractMatcher()
    private MatchResultHandler matchesHandler = new MatchResultHandler()

    boolean checkContracts(List<Class> apis, List<String> urls) {
        List<Contract> contracts = service.downloadContracts(urls)
        List<ContractoMethod> retrofitMethods = methodExtractor.findMethods(apis)
        MatchResult matchResult = matcher.calculateMatchResult(retrofitMethods, contracts)
        return matchesHandler.isSuccessfullyMatched(matchResult)
    }

}