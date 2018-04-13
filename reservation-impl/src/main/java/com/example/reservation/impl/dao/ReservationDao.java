package com.example.reservation.impl.dao;

import akka.Done;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.persistence.jpa.JpaSession;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ReservationDao {
  private final JpaSession jpa;

  @Inject
  public ReservationDao(JpaSession jpa) {
    this.jpa = jpa;
  }

  public CompletionStage<Done> addReservation(ReservationEntity reservation) {
    // Validate reservation dates
    LocalDate now = LocalDate.now();
    if (reservation.getCheckout().isBefore(reservation.getCheckin()) || reservation.getCheckout().equals(reservation.getCheckin())) {
      throw new BadRequest("Checkout date must be after checkin date");
    } else if (reservation.getCheckin().isBefore(now)) {
      throw new BadRequest("Cannot make a reservation for the past");
    }

    return jpa.withTransaction(em -> {

      if (hasConflictingReservations(em, reservation)) {
        throw new BadRequest("Listing is already booked for those dates");
      } else {
        em.persist(reservation);
      }

      return Done.getInstance();
    });
  }

  public CompletionStage<List<ReservationEntity>> getCurrentReservations(UUID listingId) {
    return jpa.withTransaction(em ->
      em.createNamedQuery("currentReservations", ReservationEntity.class)
          .setParameter("listingId", listingId)
          .setParameter("now", LocalDate.now())
          .getResultList()
    );
  }

  private boolean hasConflictingReservations(EntityManager em, ReservationEntity reservation) {
    return em.createNamedQuery("hasConflicts", Long.class)
        .setParameter("listingId", reservation.getListingId())
        .setParameter("checkin", reservation.getCheckin())
        .setParameter("checkout", reservation.getCheckout())
        .getSingleResult() > 0;
  }


}
