package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Type
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class ZoneTypeGetAllByAuditUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
    UseCase<Int, List<Type>>(schedulers) {

    override fun buildObservable(params: Int?): Observable<List<Type>> {
        return auditGateway.getAuditScopeByAudit(params!!)
    }

}