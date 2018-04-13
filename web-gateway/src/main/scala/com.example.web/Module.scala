package com.example.web

import com.example.reservation.api.ReservationService
import com.example.search.api.SearchService
import com.google.inject.AbstractModule
import com.lightbend.lagom.javadsl.client.ServiceClientGuiceSupport

class Module extends AbstractModule with ServiceClientGuiceSupport {
  override def configure(): Unit = {
    bindClient(classOf[SearchService])
    bindClient(classOf[ReservationService])
  }
}
