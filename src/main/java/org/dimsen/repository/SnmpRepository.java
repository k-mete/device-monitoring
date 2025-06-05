package org.dimsen.repository;

import org.dimsen.model.SnmpEntry;

public interface SnmpRepository {

    void saveData(SnmpEntry snmpEntry);
}
