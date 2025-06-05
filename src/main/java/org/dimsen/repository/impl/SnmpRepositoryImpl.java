package org.dimsen.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.dimsen.model.SnmpEntry;
import org.dimsen.repository.SnmpRepository;

@ApplicationScoped
public class SnmpRepositoryImpl implements SnmpRepository {

    @Override
    @Transactional
    public void saveData(SnmpEntry snmpEntry) {
        SnmpEntry.persist(snmpEntry);
    }
}
