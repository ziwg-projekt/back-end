package pl.ziwg.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class AppointmentDateService {
    public static LocalDateTime getNextValidAppointmentDateFromGivenDate(LocalDateTime date){
        if (date.getHour() >= 15) {
            return getAvailableInNextDay(date);
        } else if(date.getHour() < 7){
            return LocalDateTime.of(date.getYear(),
                    date.getMonth(), date.getDayOfMonth(), 7, 0, 0);
        } else{
            int hours = date.getHour();
            int minutes = date.getMinute();
            if(minutes % 5 != 0){
                if(minutes > 55) {
                    hours += 1;
                    minutes = 0;
                } else {
                    int remainder = minutes % 5;
                    minutes += (5 - remainder);
                }
            }
            return LocalDateTime.of(date.getYear(),
                    date.getMonth(), date.getDayOfMonth(), hours, minutes, 0);
        }
    }

    public static LocalDateTime getNextAppointmentDate(LocalDateTime lastAppointmentDate){
        LocalDateTime appointmentDate;
        if (lastAppointmentDate.getHour() >= 15) {
            appointmentDate = getAvailableInNextDay(lastAppointmentDate);
        } else if(lastAppointmentDate.getHour() < 7){
            appointmentDate = LocalDateTime.of(lastAppointmentDate.getYear(),
                    lastAppointmentDate.getMonth(), lastAppointmentDate.getDayOfMonth(), 7, 0, 0);
        } else {
            appointmentDate = lastAppointmentDate.plusMinutes(5);
        }
        return appointmentDate;
    }

    private static LocalDateTime getAvailableInNextDay(LocalDateTime last){
        LocalDateTime localDateTime = LocalDateTime.of(last.getYear(),
                last.getMonth(), last.getDayOfMonth(), 7, 0, 0);
        if(last.getDayOfWeek() == DayOfWeek.FRIDAY){
            localDateTime = localDateTime.plusDays(3);
        } else{
            localDateTime = localDateTime.plusDays(1);
        }

        return localDateTime;
    }

}
