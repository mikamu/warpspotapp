package de.warpspot.dw.poc.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistry {

	private static final ServiceRegistry INSTANCE = new ServiceRegistry();

	private Logger logger = LoggerFactory.getLogger(getClass());
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Map<Class<?>, Registrable<?>> serviceMap = new HashMap<Class<?>, Registrable<?>>();

	public static ServiceRegistry get() {
		return INSTANCE;
	}
	
	/**
	 * liefert denjenigen Service zurück, der das angeforderte Interface implementiert.
	 * 
	 * @param <ServiceType> Typvariable für den angeforderten Servicetyp.
	 * @param pServiceTypeClass Klasseninstanz des angeforderten Serviceinterfaces
	 * @return zugehörige Serviceinstanz bzw. null, falls kein passender Service vorhanden ist.
	 */
	@SuppressWarnings("unchecked")
	public <ServiceType> Optional<ServiceType> getService(final Class<ServiceType> pServiceTypeClass) {
		this.lock.readLock().lock();
		try {
			return Optional.ofNullable((ServiceType) this.serviceMap.get(pServiceTypeClass));
		} finally {
			this.lock.readLock().unlock();
		}
	}
	
	/**
	 * fügt den übergebenen Service zur ServiceMap hinzu.
	 * 
	 * @param pReg zu registrierender Service.
	 */
	public void addService(final Registrable<?> pReg) {
		this.lock.writeLock().lock();
		try {
			final Class<?> serviceType = retrieveServiceType(pReg);
			if (serviceType != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("addService(): service type name = " + serviceType.getName());
				}
				this.serviceMap.put(serviceType, pReg);
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * entfernt den übergebenen Service aus der ServiceMap.
	 * 
	 * @param pReg zu de-registrierender Service.
	 */
	public void removeService(Registrable<?> pReg) {
		this.lock.writeLock().lock();
		try {
			final Class<?> serviceType = retrieveServiceType(pReg);
			if (serviceType != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("removeService(): service type name = " + serviceType.getName());
				}
				this.serviceMap.remove(serviceType);
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	private Class<?> retrieveServiceType(Registrable<?> pReg) {
		Class<?> serviceType = null;
		Map<Class<?>, List<Class<?>>> parameterizedTypes = TypeExtractor.getInstance().extractTypes(pReg.getClass());
		if (parameterizedTypes != null) {
			List<Class<?>> staticallyReferenceableType = parameterizedTypes.get(Registrable.class);
			if (staticallyReferenceableType != null) {
				serviceType = staticallyReferenceableType.get(0);
			}
		}
		return serviceType;
	}
}
