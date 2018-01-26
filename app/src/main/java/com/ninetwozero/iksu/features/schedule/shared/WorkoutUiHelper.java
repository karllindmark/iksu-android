package com.ninetwozero.iksu.features.schedule.shared;

import android.content.Context;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.filter.ScheduleFilterItem;
import com.ninetwozero.iksu.models.Workout;

public class WorkoutUiHelper {
    public String getActionTextForWorkout(final Context context, final Workout workout) {
        return getActionTextForWorkout(context, workout, true);
    }

    public String getActionTextForWorkout(final Context context, final Workout workout, final boolean showCountIfApplicable) {
        if (showCountIfApplicable) {
            return workout.getBookedSlotCount() + "/" + workout.getTotalSlotCount();
        }

        if (workout.getReservationId() != 0) {
            if (workout.hasCheckedIn()) {
                return context.getString(R.string.label_checked_in);
            } else {
                return context.getString(R.string.label_reserved);
            }
        } else if (workout.isDropin()) {
            return context.getString(R.string.label_dropin);
        } else if (workout.isOpenForReservations()) {
            if (workout.getBookedSlotCount() < workout.getTotalSlotCount()) {
                return context.getString(R.string.label_reserve);
            } else if (workout.getBookedSlotCount() >= workout.getTotalSlotCount()) {
                return context.getString(R.string.label_full);
            } else if (workout.getReservationDeadline() > System.currentTimeMillis()) {
                return context.getString(R.string.label_info);
            }
        } else if (System.currentTimeMillis() >= workout.getEndDate()) {
            return context.getString(R.string.label_class_started);
        }
        return context.getString(R.string.empty);
    }

    public int getColorForStatusBadge(Workout workout) {
        if (workout != null) {
            if (workout.getReservationId() != 0) {
                return R.color.colorAccentLight;
            } else if (workout.isDropin()) {
                return R.color.class_state_lt60;
            } else if (workout.isOpenForReservations()) {
                if (workout.getTotalSlotCount() > 0) {
                    final double fraction = workout.getBookedSlotCount() / (workout.getTotalSlotCount() * 1.0f);
                    if (fraction < 0.75) {
                        return R.color.class_state_lt60;
                    } else if (fraction >= 0.75 && fraction < 1) {
                        return R.color.class_state_lt80;
                    } else if (fraction >= 1) {
                        return R.color.darker_grey;
                    } else {
                        return R.color.grey;
                    }
                }
            }
        }
        return R.color.grey;
    }

    public int getSecondaryActionTextForWorkout(final Workout workout) {
        if (workout.hasReservation()) {
            if (workout.hasCheckedIn()) {
                return R.string.label_checked_in;
            } else {
                return R.string.label_check_in_q;
            }
        }

        if (workout.isOpenForReservations()) {
            if (workout.isMonitoring()) {
                return R.string.label_monitoring;
            } else if (workout.isFullyBooked()) {
                return R.string.label_monitor_q;
            }
        }

        return R.string.empty;
    }

    public boolean shouldShowTheSecondayAction(Workout workout) {
        return (
            (workout.isFullyBooked() && workout.isOpenForReservations() && !workout.hasReservation()) ||
                (workout.hasReservation() && !workout.hasCheckedIn())
        );
    }

    public int getTitleForFilter(final String id, final int type) {
        switch (type) {
            case ScheduleFilterItem.ROW_FILTER_LOCATION:
                return getTitleForLocationFilter(id);
            case ScheduleFilterItem.ROW_FILTER_TYPE:
                return getTitleForTypeFilter(id);
            case ScheduleFilterItem.ROW_FILTER_TIME_OF_DAY:
            default:
                return R.string.todo;
        }
    }

    private int getTitleForTypeFilter(final String id) {
        switch (id) {
            case "b_bs":
                return R.string.type_title_bollsport;
            case "b_bgt":
                return R.string.type_title_beach;
            case "g_klätt":
                return R.string.type_title_climbing;
            case "g_yo":
                return R.string.type_title_yoga;
            case "g_iw":
                return R.string.type_title_indoor_walking;
            case "g_tt":
                return R.string.type_title_total_training;
            case "g_cy":
                return R.string.type_title_cykel;
            case "g_cx":
                return R.string.type_title_cxworx;
            case "g_aq":
                return R.string.type_title_aqua;
            case "g_st":
                return R.string.type_title_step;
            case "g_pirepp":
            case "g_pitrx":
                return R.string.type_title_pilates_reformer;
            case "g_bp":
                return R.string.type_title_bodypump;
            case "g_gy":
                return R.string.type_title_gympa;
            case "g_pi":
                return R.string.type_title_pilates;
            case "g_grp":
                return R.string.type_title_grit;
            case "g_bb":
                return R.string.type_title_bodybalance;
            case "g_me":
                return R.string.type_title_member_happenings;
            case "g_bj":
                return R.string.type_title_bodyjam;
            case "g_fb":
                return R.string.type_title_for_barn;
            case "g_zu":
                return R.string.type_title_zumba;
            case "g_bs":
                return R.string.type_title_bodystep;
            case "g_sh":
                return R.string.type_title_shbam;
            case "g_bc":
                return R.string.type_title_bodycombat;
            case "g_mojo":
                return R.string.type_title_mojo_flex;
            case "g_squa":
                return R.string.type_title_squash;
            case "g_pu":
                return R.string.type_title_punchout;
            case "g_ae":
                return R.string.type_title_dance;
            case "g_soma":
                return R.string.type_title_soma_move;
            case "g_fp":
                return R.string.type_title_mamma_baby;
            case "g_bv":
                return R.string.type_title_bodyvive;
            case "g_bamo":
                return R.string.type_title_barre;
            case "gy_cirkel":
                return R.string.type_title_cirkeltraning;
            case "g_cc":
                return R.string.type_title_core_control;
            case "gy_löp":
                return R.string.type_title_lopfokus;
            case "gy_stav":
                return R.string.type_title_stavgang;
            default:
                return R.string.type_title_label_unknown;
        }
    }

    private int getTitleForLocationFilter(final String id) {
        switch (id) {
            case "facility_sport":
                return R.string.facility_sport;
            case "facility_spa":
                return R.string.facility_spa;
            case "facility_plus":
                return R.string.facility_plus;
            default:
                return R.string.facility_unknown;
        }
    }

    public int getDescriptionForWorkout(final Workout workout) {
        switch (workout.getType()) {
            case "b_bgt":
                return R.string.type_description_beach;
            case "b_bs":
                switch(workout.getKey()) {
                    case "BSIBM":
                        return R.string.type_description_innebandy;
                    case "BSBAM":
                        return R.string.type_description_basketball;
                    case "BSVBM":
                        return R.string.type_description_volleyball;
                    case "BSFOM":
                        return R.string.type_description_fotboll;
                    default:
                        return R.string.todo;
                }
            case "g_ae":
                return R.string.type_description_dance;
            case "g_aq":
                switch (workout.getKey()) {
                    case "AQBA":
                        return R.string.type_description_aqua_balte;
                    case "AQTT":
                        return R.string.type_description_aqua_tt;
                    case "AQBO":
                        return R.string.type_description_aqua_box;
                    case "AQ":
                    default:
                        return R.string.type_description_aqua;
                }
            case "g_bamo":
                return R.string.type_description_barre;
            case "g_bb":
                return R.string.type_description_bodybalance;
            case "g_bc":
                return R.string.type_description_bodycombat;
            case "g_bj":
                return R.string.type_description_bodyjam;
            case "g_bp":
                return R.string.type_description_bodypump;
            case "g_bs":
                return R.string.type_description_bodystep;
            case "g_bv":
                return R.string.type_description_bodyvive;
            case "g_cc":
                return R.string.type_description_core_control;
            case "g_cx":
                return R.string.type_description_cxworx;
            case "g_cy": // "Cykel/Cykel WATT/RPM®/SPRINT™"
                return R.string.type_description_cykel;
            case "g_fb": // "För barn"
                switch (workout.getKey()) {
                    case "GYFAMG":
                        return R.string.type_description_familjegympa;
                    case "GYFAMS":
                        return R.string.type_description_familjesim;
                    default:
                        return R.string.todo;
                }
            case "g_fp":
                return R.string.type_description_mamma_baby;
            case "g_grp":
                return R.string.type_description_grit_strength;
            case "g_gy":
                return R.string.type_description_gympa;
            case "g_iw":
                return R.string.type_description_indoor_walking;
            case "g_klätt": // KLättring
                return R.string.type_description_klattring;
            case "g_me": // Medlemsaktivitet och happenings
                return R.string.todo;
            case "g_mojo":
                return R.string.type_description_mojo_flex;
            case "g_pi":
                if (workout.getKey().equals("MIPICI45")) {
                    return R.string.type_description_pilates_cirkel;
                } else {
                    return R.string.type_description_pilates;
                }
            case "g_pirepp":
            case "g_pitrx":
                if (workout.getKey().equals("MIREFORM")) {
                    return R.string.type_description_pilates_reformer_rehab;
                } else {
                    return R.string.type_description_pilates_reformer;
                }
            case "g_pu":
                return R.string.type_description_punchout;
            case "g_sh":
                return R.string.type_description_shbam;
            case "g_soma":
                return R.string.type_description_soma_move;
            case "g_squa": // Söndagssquash
                return R.string.type_description_squash;
            case "g_st":
                switch (workout.getKey()) {
                    case "AESD":
                        return R.string.type_description_stepdance;
                    case "AESM":
                        return R.string.type_description_stepmuskel;
                    case "AESB":
                        return R.string.type_description_stepbas;
                    default:
                        return R.string.type_description_step;
                }
            case "g_tt":
                return R.string.type_description_total_training;
            case "g_yo":
                switch (workout.getKey()) {
                    case "MILAYO":
                        return R.string.type_description_yoga_latt;
                    case "MIYIYO":
                    case "MIYIYO90":
                        return R.string.type_description_yoga_yin;
                    case "MIFULYOBAS":
                        return R.string.type_description_yoga_mindful_bas;
                    case "MIFULYO":
                        return R.string.type_description_yoga_mindful;
                    case "MIYO":
                    default:
                        return R.string.type_description_yoga;
                }
            case "g_zu":
                return R.string.type_description_zumba;
            case "gy_cirkel":
                return R.string.type_description_cirkeltraning;
            case "gy_löp": // Löpfokus
                return R.string.todo;
            case "gy_stav": // Stavgång
                return R.string.todo;
            default:
                return R.string.todo;
        }
    }
}
