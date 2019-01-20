package com.gemini.energy.service

import android.content.Context
import com.gemini.energy.App
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.ELightingType
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.service.device.General
import com.gemini.energy.service.device.Hvac
import com.gemini.energy.service.device.Motors
import com.gemini.energy.service.device.lighting.Cfl
import com.gemini.energy.service.device.lighting.Halogen
import com.gemini.energy.service.device.lighting.Incandescent
import com.gemini.energy.service.device.lighting.LinearFluorescent
import com.gemini.energy.service.device.plugload.*
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate


abstract class ComputableFactory {
    abstract fun build(): IComputable

    companion object {
        lateinit var computable: Computable<*>
        inline fun createFactory(computable: Computable<*>, utilityRateGas: UtilityRate,
                                 utilityRateElectricity: UtilityRate,
                                 usageHours: UsageHours, outgoingRows: OutgoingRows,
                                 context: Context): ComputableFactory {
            this.computable = computable

            // This is to ensure every computable gets it's own copy of Utility Rate
            // Not sure how to create these instances from Dagger
            val _utilityRateGas = UtilityRate(App.instance)
            val _utilityRateElectricity = UtilityRate(App.instance)

            return when (computable.auditScopeType as EZoneType) {

                EZoneType.Plugload                  -> PlugloadFactory(_utilityRateGas,
                        _utilityRateElectricity, usageHours, outgoingRows, context)

                EZoneType.HVAC                      -> HvacFactory(_utilityRateGas,
                        _utilityRateElectricity, usageHours, outgoingRows, context)

                EZoneType.Lighting                  -> LightingFactory(_utilityRateGas,
                        _utilityRateElectricity, usageHours, outgoingRows, context)

                EZoneType.Motors                    -> MotorFactory(_utilityRateGas,
                        _utilityRateElectricity, usageHours, outgoingRows, context)

                EZoneType.Others                   -> GeneralFactory()
            }
       }
    }
}

class PlugloadFactory(private val utilityRateGas: UtilityRate,
                      private val utilityRateElectricity: UtilityRate,
                      private val usageHours: UsageHours,
                      private val outgoingRows: OutgoingRows,
                      private val context: Context) : ComputableFactory() {

    override fun build(): IComputable {
        return when(computable.auditScopeSubType as EApplianceType) {

            EApplianceType.CombinationOven          -> CombinationOven(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows)

            EApplianceType.ConvectionOven           -> ConvectionOven()
            EApplianceType.ConveyorOven             -> ConveyorOven()
            EApplianceType.Fryer                    -> Fryer()

            EApplianceType.IceMaker                 -> IceMaker(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            EApplianceType.RackOven                 -> RackOven()

            EApplianceType.Refrigerator             -> Refrigerator(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows)

            EApplianceType.SteamCooker              -> SteamCooker()

            EApplianceType.Griddle                  -> Griddle(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            EApplianceType.HotFoodCabinet           -> HotFoodCabinet(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            EApplianceType.ConveyorBroiler          -> ConveyorBroiler(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            EApplianceType.DishWasher               -> DishWasher(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            EApplianceType.PreRinseSpray            -> PreRinseSpray(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

        }
    }

}

class LightingFactory(private val utilityRateGas: UtilityRate,
                      private val utilityRateElectricity: UtilityRate,
                      private val usageHours: UsageHours,
                      private val outgoingRows: OutgoingRows,
                      private val context: Context) : ComputableFactory() {
    override fun build(): IComputable {
        return when(computable.auditScopeSubType as ELightingType) {

            ELightingType.CFL                       -> Cfl(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            ELightingType.Halogen                   -> Halogen(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            ELightingType.Incandescent              -> Incandescent(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

            ELightingType.LinearFluorescent         -> LinearFluorescent(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

        }
    }
}

class HvacFactory(private val utilityRateGas: UtilityRate,
                  private val utilityRateElectricity: UtilityRate,
                  private val usageHours: UsageHours,
                  private val outgoingRows: OutgoingRows,
                  private val context: Context) : ComputableFactory() {

    override fun build(): IComputable = Hvac(computable,
            utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

}

class MotorFactory(private val utilityRateGas: UtilityRate,
                   private val utilityRateElectricity: UtilityRate,
                   private val usageHours: UsageHours,
                   private val outgoingRows: OutgoingRows,
                   private val context: Context) : ComputableFactory() {

    override fun build() = Motors(computable,
            utilityRateGas, utilityRateElectricity, usageHours, outgoingRows, context)

}

class GeneralFactory : ComputableFactory() {
    override fun build() = General()
}