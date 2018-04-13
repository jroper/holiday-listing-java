package com.example.reservation.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.example.reservation.api.Reservation;
import com.example.reservation.api.ReservationAdded;
import com.example.reservation.api.ReservationService;
import com.example.reservation.impl.dao.ReservationDao;
import com.example.reservation.impl.dao.ReservationEntity;
import com.example.search.api.SearchService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservationServiceImpl implements ReservationService {

  private final ReservationDao dao;
  private final SearchService searchService;

  @Inject
  public ReservationServiceImpl(ReservationDao dao, SearchService searchService) {
    this.dao = dao;
    this.searchService = searchService;
  }

  @Override
  public ServiceCall<Reservation, ReservationAdded> reserve(UUID listingId) {
    return reservation -> {
      UUID reservationId = UUID.randomUUID();
      ReservationEntity entity = new ReservationEntity(reservationId, listingId, reservation.getCheckin(), reservation.getCheckout());
      return dao.addReservation(entity)
          .thenCompose(done -> {
            ReservationAdded added = new ReservationAdded(reservationId, listingId, reservation);
            return searchService.reservationAdded().invoke(added).thenApply(d -> added);
          });
    };
  }

  @Override
  public ServiceCall<NotUsed, List<Reservation>> getCurrentReservations(UUID listingId) {
    return notUsed -> dao.getCurrentReservations(listingId)
        .thenApply(reservations ->
            reservations.stream()
                .map(entity -> new Reservation(entity.getCheckin(), entity.getCheckout())
                ).collect(Collectors.toList())
        );
  }

  @Override
  public Topic<ReservationAdded> reservationEvents() {
    return TopicProducer.singleStreamWithOffset(offset -> Source.maybe());
  }
}
