package com.javainsurecore.service;

import com.javainsurecore.model.Insurance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

/** Administra en memoria la colección de pólizas registradas. */
public class InsurancePortfolio {
    private static final Logger LOGGER = LogManager.getLogger(InsurancePortfolio.class);
    private final SequencedCollection<Insurance> policies = new ArrayList<>();

    /** Agrega una póliza y registra la operación en el log. */
    public synchronized void add(Insurance insurance) {
        policies.add(insurance);
        LOGGER.info("Policy created successfully for client {}", insurance.client().id());
    }

    /** @return copia inmutable de las pólizas en su orden de registro */
    public synchronized List<Insurance> listAll() {
        return policies.stream().toList();
    }

    /**
     * Busca pólizas por ID de cliente sin distinguir mayúsculas y minúsculas.
     *
     * @param clientId identificador del cliente que se busca
     * @return pólizas asociadas al cliente
     */
    public synchronized List<Insurance> findByClientId(String clientId) {
        return policies.stream()
                .filter(policy -> policy.client().id().equalsIgnoreCase(clientId))
                .toList();
    }

    /** @return cantidad de pólizas registradas */
    public synchronized int size() {
        return policies.size();
    }
}
