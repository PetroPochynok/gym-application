package com.epam.gym.crm.repository.specification;

import com.epam.gym.crm.model.Training;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.time.LocalDate;

public class TrainingSpecification {

    public static Specification<Training> filter(
            String traineeUsername,
            LocalDate from,
            LocalDate to,
            String trainerUsername,
            String trainingType
    ) {
        return (root, query, cb) -> cb.and(
                equalIfNotNull(cb, root.get("trainee").get("username"), traineeUsername),
                equalIfNotNull(cb, root.get("trainer").get("username"), trainerUsername),
                equalIfNotNull(cb, root.get("trainingType").get("trainingTypeName"), trainingType),
                greaterOrEqualIfNotNull(cb, root.get("trainingDate"), from),
                lessOrEqualIfNotNull(cb, root.get("trainingDate"), to)
        );
    }

    private static Predicate equalIfNotNull(
            CriteriaBuilder cb,
            Path<?> path,
            Object value
    ) {
        return value == null ? cb.conjunction() : cb.equal(path, value);
    }

    private static Predicate greaterOrEqualIfNotNull(
            CriteriaBuilder cb,
            Path<?> path,
            LocalDate value
    ) {
        return value == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(path.as(LocalDate.class), value);
    }

    private static Predicate lessOrEqualIfNotNull(
            CriteriaBuilder cb,
            Path<?> path,
            LocalDate value
    ) {
        return value == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(path.as(LocalDate.class), value);
    }
}