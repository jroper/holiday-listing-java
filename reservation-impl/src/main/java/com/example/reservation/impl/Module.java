package com.example.reservation.impl;

import com.example.reservation.api.ReservationService;
import com.example.search.api.SearchService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class Module extends AbstractModule implements ServiceGuiceSupport {

  @Override
  protected void configure() {
    bindService(ReservationService.class, ReservationServiceImpl.class);
    bindClient(SearchService.class);
  }
}
