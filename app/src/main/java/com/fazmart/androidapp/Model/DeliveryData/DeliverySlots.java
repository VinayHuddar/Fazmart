package com.fazmart.androidapp.Model.DeliveryData;

/**
 * Created by Vinay on 13-06-2015.
 */
public class DeliverySlots {
    DeliveryDays[] delivery_slots;

    class DeliveryDays {
        int id;
        String name;
        String date;
        int total;
        int available;
        DeliverySlots.SlotGroups slots;
    }


    class SlotGroups {
        Slot[] morning;
        Slot[] noon;
        Slot[] evening;
    }

    class Slot {
        int id;
        String name;
        int hour;
        boolean available;
    }

    public String[] GetDeliveryDayNames() {
        int numDays = delivery_slots.length;
        String[] dayNames = new String[numDays];
        for (int i = 0; i < numDays; i++)
            dayNames[i] = delivery_slots[i].name;

        return dayNames;
    }

    public String[] GetSlotNames(int dayIdx) {
        Slot[] morningSlots = delivery_slots[dayIdx].slots.morning;
        Slot[] noonSlots = delivery_slots[dayIdx].slots.noon;
        Slot[] eveningSlots = delivery_slots[dayIdx].slots.evening;

        int numSlots = morningSlots.length + noonSlots.length + eveningSlots.length;

        String[] slotNames = new String[numSlots];
        for (int i = 0; i < morningSlots.length; i++)
            slotNames[i] = morningSlots[i].name;
        for (int i = 0, j = morningSlots.length; i < noonSlots.length; i++, j++)
            slotNames[j] = noonSlots[i].name;
        for (int i = 0, j = morningSlots.length + noonSlots.length; i < eveningSlots.length; i++, j++)
            slotNames[j] = eveningSlots[i].name;

        return slotNames;
    }

    public boolean AreSlotsAvailable(int dayIdx) {
        return delivery_slots[dayIdx].available > 0;
    }

    private Slot GetSlot (int dayIdx, int slotIdx) {
        SlotGroups slots = delivery_slots[dayIdx].slots;

        int morningslotCnt = slots.morning.length;
        if (slotIdx < morningslotCnt)
            return slots.morning[slotIdx];

        int noonSlotCnt = slots.noon.length;
        if (slotIdx < (morningslotCnt + noonSlotCnt))
            return slots.noon[slotIdx - morningslotCnt];

        return slots.evening[slotIdx - (morningslotCnt + noonSlotCnt)];
    }

    public boolean IsSlotAvailable(int dayIdx, int slotIdx) {
        return GetSlot(dayIdx, slotIdx).available;
    }

    public String GetSlotName(int dayIdx, int slotIdx) {
        return GetSlot(dayIdx, slotIdx).name;
    }

    public int GetSlotId(int dayIdx, int slotIdx) {
        return GetSlot(dayIdx, slotIdx).id;
    }

    public int GetSlotCount (int dayIdx) {
        SlotGroups slots = delivery_slots[dayIdx].slots;
        return slots.morning.length + slots.noon.length + slots.evening.length;
    }

}
