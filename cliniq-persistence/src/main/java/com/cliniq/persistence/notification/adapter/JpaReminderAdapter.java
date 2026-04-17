package com.cliniq.persistence.notification.adapter;

import com.cliniq.application.notification.port.out.ReminderRepository;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.persistence.notification.entity.JpaReminder;
import com.cliniq.persistence.notification.mapper.JpaReminderMapper;
import com.cliniq.persistence.notification.repository.JpaReminderRepository;
import com.cliniq.shared.domain.TenantId;

import java.util.List;
import java.util.Optional;

public class JpaReminderAdapter implements ReminderRepository {

    private final JpaReminderRepository jpaReminderRepository;

    public JpaReminderAdapter(JpaReminderRepository jpaReminderRepository) {
        this.jpaReminderRepository = jpaReminderRepository;
    }

    @Override
    public void save(Reminder reminder) {
        JpaReminder jpa = JpaReminderMapper.toJpa(reminder);
        jpaReminderRepository.save(jpa);
    }

    @Override
    public List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId) {
        return jpaReminderRepository.findByTenantIdAndAppointmentId(tenantId.value(), appointmentId.value())
                .stream()
                .map(JpaReminderMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Reminder> findById(TenantId tenantId, ReminderId id) {
        return jpaReminderRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(JpaReminderMapper::toDomain);
    }
}
