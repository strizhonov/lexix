package com.strizhonovapps.lexicaapp.di

import com.strizhonovapps.lexicaapp.notification.NotificationJobReceiver
import com.strizhonovapps.lexicaapp.notification.WakeUpNotificationJobReceiver
import com.strizhonovapps.lexicaapp.view.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [BeanModule::class])
interface DiComponent {
    fun inject(activity: BaseWordManipulationActivity)
    fun inject(activity: TrainingActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: FromFileSavingBackgroundActivity)
    fun inject(activity: NotificationJobReceiver)
    fun inject(fragment: StatsFragment)
    fun inject(fragment: PreTrainingFragment)
    fun inject(fragment: WordListFragment)
    fun inject(receiver: WakeUpNotificationJobReceiver)
}
