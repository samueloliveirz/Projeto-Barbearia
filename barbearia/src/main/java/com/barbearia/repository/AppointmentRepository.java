package com.barbearia.repository;

import com.barbearia.enums.AppointmentStatus;
import com.barbearia.enums.PaymentStatus;
import com.barbearia.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBarbershopId(Long barbershopId);

    Optional<Appointment> findByIdAndBarbershopId(Long id, Long barbershopId);

    List<Appointment> findByDateTimeBetweenAndBarbershopId(
            LocalDateTime start, LocalDateTime end, Long barbershopId);

    List<Appointment> findByDateTimeBetweenAndBarberId(
            LocalDateTime start, LocalDateTime end, Long barberId);

    boolean existsByDateTimeAndBarberIdAndStatusNot(
            LocalDateTime dateTime, Long barberId, AppointmentStatus status);

    List<Appointment> findByPaymentStatusAndBarbershopId(
            PaymentStatus paymentStatus, Long barbershopId);

    List<Appointment> findByClient_PhoneAndBarbershopId(String phone, Long barbershopId);

    @Query("SELECT COALESCE(SUM(a.value), 0) FROM Appointment a " +
            "WHERE a.paymentStatus = :paymentStatus AND a.barbershop.id = :barbershopId")
    BigDecimal sumValueByPaymentStatusAndBarbershopId(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("barbershopId") Long barbershopId);
}