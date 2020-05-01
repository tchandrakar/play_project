package modules

import Utilities.db.DbUtils
import com.google.inject.AbstractModule

class BindModule extends AbstractModule {

  override def configure(): Unit = {
    DbUtils.checkAndCreateTables
  }
}
