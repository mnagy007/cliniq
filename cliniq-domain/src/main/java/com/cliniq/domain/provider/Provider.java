package com.cliniq.domain.provider;

import com.cliniq.shared.domain.AggregateRoot;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.shared.validation.Preconditions;
import com.cliniq.domain.appointment.TimeSlot;

import java.util.List;

public class Provider extends AggregateRoot<ProviderId> {
    private final ProviderId id;
    private final TenantId tenantId;
    private ProviderName name;
    private Specialty specialty;
    private List<AvailabilitySlot> availabilitySlots;


    private Provider(ProviderId id,
                     TenantId tenantId,
                     ProviderName name,
                     Specialty specialty,
                     List<AvailabilitySlot> availabilitySlots) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.specialty = specialty;
        this.availabilitySlots = List.copyOf(availabilitySlots);
    }

     public static Provider create(TenantId tenantId,
                                   ProviderName name,
                                   Specialty specialty,
                                   List<AvailabilitySlot> availabilitySlots) {

         Preconditions.requireNonNull(tenantId, "tenantId");
         Preconditions.requireNonNull(name, "name");
         Preconditions.requireNonNull(specialty, "specialty");
         Preconditions.requireNonNull(availabilitySlots, "availabilitySlots");

        Provider provider = new Provider(
                ProviderId.generate(), tenantId, name, specialty, List.copyOf(availabilitySlots));
        return provider;
    }

    public boolean isAvailable(TimeSlot timeSlot) {
        return availabilitySlots.stream().anyMatch(slot ->
                slot.dayOfWeek() == timeSlot.date().getDayOfWeek()
                        && !timeSlot.startTime().isBefore(slot.startTime())
                        && !timeSlot.endTime().isAfter(slot.endTime())
        );
    }

    public void syncFrom(ProviderName name,  Specialty specialty,  List<AvailabilitySlot> availabilitySlots) {
        Preconditions.requireNonNull(name, "name");
        Preconditions.requireNonNull(specialty, "specialty");
        Preconditions.requireNonNull(availabilitySlots, "availabilitySlots");

        this.name = name;
        this.specialty = specialty;
        this.availabilitySlots = List.copyOf(availabilitySlots);
    }

    @Override
    public ProviderId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public ProviderName getName() {
        return name;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public List<AvailabilitySlot> getAvailabilitySlots() {
        return availabilitySlots;
    }
}
