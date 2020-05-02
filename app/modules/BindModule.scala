package modules

import com.google.inject.AbstractModule
import utilities.DbUtils


class BindModule extends AbstractModule {

  override def configure(): Unit = {
    DbUtils.checkAndCreateTables
  }
}