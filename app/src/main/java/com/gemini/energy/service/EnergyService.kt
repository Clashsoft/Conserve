
package com.gemini.energy.service

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.gemini.energy.WordDocumentGenerator
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.merge
import timber.log.Timber


class EnergyService(
        private val context: Context,
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway,
        private val utilityRateElectricity: UtilityRate,
        private val usageHours: UsageHours,
        private val outgoingRows: OutgoingRows) {

    /**
     * Holds the Unit of Work i.e the IComputables
     * */
    private var taskHolder: MutableList<Observable<Computable<*>>> = mutableListOf()


    /**
     * Holds the reference to the Observed Stream
     * */
    private var disposables: MutableList<Disposable> = mutableListOf()


    /**
     * Energy UtilityRate Gas - Electricity
     * */
    private val energyUtilityGas = UtilityRate(context)


    private var ebases = mutableListOf<EBase>()

    /**
     * Energy Calculation - Main Entry Point
     * */
    fun run(callback: (status: Boolean) -> Unit) {

        cleanup()

        val welcome =

                "------------------------------------\n" +
                ":::: Gemini Energy - Crunch Inc ::::\n" +
                "------------------------------------\n"

        Timber.d(welcome)

        disposables.add(auditGateway.getComputable()
                .subscribeOn(schedulers.subscribeOn)
                .subscribe { computables ->
                    Observable.fromIterable(computables)
                            .subscribe({ eachComputable ->
                                Timber.d("**** Computables Iterable - (${thread()}) ****")
                                Timber.d(eachComputable.toString())
                                getComputable(eachComputable)
                                        .subscribe({
                                            synchronized(taskHolder) {
                                                taskHolder.add(it)
                                            }
                                        }, { exception ->

                                            Timber.d("##### Error !! Error !! Error #####")
                                            exception.printStackTrace()
                                            Crashlytics.logException(exception)

                                        }, {})
                            }, {}, {
                                Timber.d("**** Computables Iterable - [ON COMPLETE] ****")
                                doWork(callback) // << ** Executed Only One Time ** >> //
                            })
                })

    }

    /**
     * Required to Clean Up Existing Disposables before a new CRUNCH begins
     * */
    private fun cleanup() {
        Timber.d("Cleanup - (${thread()})")
        if (disposables.count() > 0) {
            Timber.d("DISPOSABLE COUNT - [${disposables.count()}] - (${thread()})")
            disposables.forEach {
                it.dispose()
                Timber.d("POST DISPOSABLE - Status - [${it.isDisposed}]")
            }
        }

        disposables.clear()
        taskHolder.clear()
    }

    /**
     * Holds the Main Work - i.e Running each Energy Calculations for each of the IComputables
     * */
//    var allValues = mutableListOf<EBase>()
    private fun doWork(callback: (status: Boolean) -> Unit) {
        Timber.d("####### DO WORK - COUNT [${taskHolder.count()}] - (${thread()}) #######")

//        val valuesForDocumentGeneration = mutableListOf<Observable<IComputable>>()

        disposables.add(taskHolder.merge()
                .observeOn(schedulers.observeOn)
                .subscribe({

                    //                    if (it.auditScopeType != null) {
//                        valuesForDocumentGeneration.add(getIComputatble(it))
//                    }

                }, { exception ->
                    exception.printStackTrace()
                    Crashlytics.logException(exception)
                    callback(false)
                }, {
                    Timber.d("**** Merge - [ON COMPLETE] ****")

                    WordDocumentGenerator().triggerGeneration(ebases)

//                    disposables.add(valuesForDocumentGeneration.merge()
//                            .subscribe ({
//                                Log.i("-----moo", "wat is it?: ${it.javaClass.name}")
//                                synchronized(ebaseValues) {
//                                    val values = ebaseValues
//
//                                    val value = it as EBase
//                                    value.compute {  }
//
//                                    allValues.add(value)
//
//                                    if (ebaseValues.any
//                                            { obj ->
//                                                obj.computable.auditScopeId == value.computable.auditScopeId
//                                                        &&
//                                                        obj.computable.auditId == value.computable.auditId
//                                            }) {
//                                    }
//                                    else {
//                                        ebaseValues.add(value)
//                                    }
//                                }
//                            }, {
//                                Log.i("-----moo", "EXCEPTION: ${it.localizedMessage}")
//                            }, {
//                                val maybes = ebaseValues
//
//                                WordDocumentGenerator().triggerGeneration(maybes)
//
//
//                                Log.i("-----moo", "DONE")
//                            }))



                    callback(true) // << ** The final Exit Point ** >> //
                }))
    }

    /**
     * Takes in two Observables
     * 1. To get the Feature Pre Audit (pre-audit)
     * 2. To get the Feature Audit Scope (feature data)
     *
     * The build() method returns the fully packaged IComputable as a Flowable - Wrapped by an Observable
     * */
    private fun getComputable(computable: Computable<*>): Observable<Observable<Computable<*>>> {

//        var value

        fun build(computable: Computable<*>, featureAuditScope: List<Feature>,
                          featurePreAudit: List<Feature>): Observable<Computable<*>>{


            computable.featureAuditScope = featureAuditScope
            computable.featurePreAudit = featurePreAudit

            val iComputable = ComputableFactory.createFactory(computable, energyUtilityGas,
                    utilityRateElectricity, usageHours,
                    outgoingRows, context).build()

//            try {
//                var value = returnable as EBase
//                ebases.add(value)
//            }
//            catch(e: Exception) {
//                print("EXCEPTION")
//            }

            iComputable.compute().blockingLast()

            // try next
            if (iComputable is EBase) {
                ebases.add(iComputable)
            } else {
                Log.i("-----moo", "not ebase")
            }

//            if (returnable.javaClass is EBase) {
//                ebases.add(returnable as EBase)
//            } else {
//                Log.i("-----moo", "not ebase")
//            }

            return iComputable.compute()
//            return ComputableFactory.createFactory(computable, energyUtilityGas,
//                    utilityRateElectricity, usageHours,
//                    outgoingRows, context).build().compute()
        }

//        Observable.zi

        return Observable.zip(
                auditGateway.getFeature(computable.auditId),
                auditGateway.getFeatureByType(computable.auditScopeId),
                BiFunction<List<Feature>, List<Feature>, Observable<Computable<*>>> { featurePreAudit, featureAuditScope ->
                    //                    Log.i("","")
                    build(computable, featureAuditScope, featurePreAudit)
                })
    }

//    private fun getIComputatble(computable: Computable<*>): Observable<IComputable> {
//        fun build(computable: Computable<*>, featureAuditScope: List<Feature>, featurePreAudit: List<Feature>): IComputable {
//            computable.featureAuditScope = featureAuditScope
//            computable.featurePreAudit = featurePreAudit
//
//            val returnable = ComputableFactory
//                    .createFactory(computable, energyUtilityGas, utilityRateElectricity, usageHours, outgoingRows, context)
//                    .build()
//            returnable.compute()
//            return returnable
//        }
//
//        return Observable.zip(
//                auditGateway.getFeature(computable.auditId),
//                auditGateway.getFeatureByType(computable.auditScopeId),
//                BiFunction<List<Feature>, List<Feature>, IComputable> { featurePreAudit, featureAuditScope ->
//                    build(computable, featureAuditScope, featurePreAudit)
//                })
//    }

    private fun thread() = Thread.currentThread().name

}
