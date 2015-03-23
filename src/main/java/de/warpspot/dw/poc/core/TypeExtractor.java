package de.warpspot.dw.poc.core;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hilfsklasse zur Ermittlung aller implementierten tats�chlichen Typen generischer Oberklassen und Interfaces. 
 * 
 * @author mm
 */
public class TypeExtractor {
	private static TypeExtractor instance = new TypeExtractor();
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * liefert die eine TypeExtractor-Instanz zur�ck.
	 * 
	 * @return TypeExtractorinstanz
	 */
	public static TypeExtractor getInstance() {
		return instance;
	}

	/**
	 * ermittelt, ob es sich bei dem �bergebenen Typ um einen generischen Typen handelt oder nicht.
	 * 
	 * @param pType zu untersuchender Typ
	 * @return true, falls es sich bei der �bergebenen Klasse um einen generischen Typen handelte, anderenfalls false.
	 */
	public boolean isTypeGeneric(Class<?> pType) {
		return pType.getTypeParameters().length > 0;
	}
	
	/**
	 * ermittelt alle implementierten tats�chlichen Typen generischer Oberklassen und Interfaces des �bergebenen
	 * Typs.<br/>
	 * 
	 * Einfaches Beispiel:<br/>
	 * <code>public MeinIterator implements Iterator<String></code>
	 * Wird MeinIterator.class dieser Methode �bergeben, so enth�lt die Ergebnismap lediglich einen Eintrag unter
	 * dem Schl�ssel java.util.Iterator.class. Der darunter abgelegte Eintrag ist eine Liste, deren einziges Element
	 * java.lang.String.class entspricht.
	 * 
	 * F�r ein weit komplexeres Beispiel der M�glichkeiten siehe bitte den zugeh�rigen Testcase
	 * de.condat.epet.util.reflect.test.TypeExtractorTestCase.
	 * 
	 * @param pType zu untersuchender Typ (muss ein konkreter Typ sein, kein generischer)
	 * @return Map, deren Schl�ssel aus der Menge aller generischen Typen stammen, die auf den �bergebenen Typen
	 *    zutreffen. Die zugeh�rigen Werte enthalten die konkreten Klassen der generischen Typen.
	 *    Falls ein generischer Typ �bergeben wurde, wird eine leere Map zur�ckgeliefert.
	 */
	public Map<Class<?>, List<Class<?>>> extractTypes(Class<?> pType) {
		Map<Class<?>, List<Class<?>>> resultMap = new LinkedHashMap<Class<?>, List<Class<?>>>();

		if (!isTypeGeneric(pType)) {
			// nur, wenn wir es mit einem konkreten Typen zu tun haben...
			if (logger.isTraceEnabled()) {
				logger.trace("class: " + pType);
			}
			Map<TypeVariable<?>, Class<?>> typeParamMap = new HashMap<TypeVariable<?>, Class<?>>();
			extractTypesInternal(pType, resultMap, typeParamMap);
			if (logger.isTraceEnabled()) {
				printResult(resultMap);
			}
		}
			
		return resultMap;
	}

	private void extractTypesInternal(Class<?> pExaminedType, Map<Class<?>, List<Class<?>>> pResultMap,
			Map<TypeVariable<?>, Class<?>> pTypeParamMap) {

		// erst die Interfaces untersuchen
		for (Type iface : pExaminedType.getGenericInterfaces()) {
			if (logger.isTraceEnabled()) {
				logger.trace("iface: " + iface);
			}
			examineType(iface, pResultMap, pTypeParamMap);
		}
		
		// dann in der Vererbungshierarchie nach oben gehen
		Type superType = pExaminedType.getGenericSuperclass();
		if (superType != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("class: " + superType);
			}
			examineType(superType, pResultMap, pTypeParamMap);
		}
	}

	private void examineType(Type pType, Map<Class<?>, List<Class<?>>> pResultMap,
			Map<TypeVariable<?>, Class<?>> pTypeParamMap) {
		
		Class<?> nextType = null;
		if (pType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) pType;
			TypeVariable<?>[] typeParams = ((GenericDeclaration) parameterizedType.getRawType()).getTypeParameters();
			Type[] types = parameterizedType.getActualTypeArguments();
			List<Class<?>> actualTypes = new ArrayList<Class<?>>();
			for (int i = 0; i < typeParams.length; i++) {
				if (logger.isTraceEnabled()) {
					logger.trace("param[" + i + "] = " + typeParams[i].getName());
				}
				if (types[i] instanceof TypeVariable<?>) {
					TypeVariable<?> typeVar = (TypeVariable<?>) types[i];
					if (logger.isTraceEnabled()) {
						logger.trace("Nicht-aufgel�ste Typvariable '" + typeVar.getName()
								+ "' vorgefunden, versuche Lookup via TypeParamMap...");
					}
					Class<?> lookupType = pTypeParamMap.get(typeVar);
					if (lookupType == null) {
						throw new IllegalStateException("Konnte verwendeten Typ einer generischen Klasse nicht ermitteln.");
					}
					actualTypes.add(lookupType);
					if (logger.isTraceEnabled()) {
						logger.trace("type[" + i + "] = " + lookupType.getName());
					}
				} else {
					Class<?> type = (Class<?>) types[i];
					pTypeParamMap.put(typeParams[i], type);
					actualTypes.add(type);
					if (logger.isTraceEnabled()) {
						logger.trace("type[" + i + "] = " + type.getName());
					}
				}
			}
			nextType = (Class<?>) parameterizedType.getRawType();
			pResultMap.put(nextType, actualTypes);
		} else {
			nextType = (Class<?>) pType;
		}
		extractTypesInternal(nextType, pResultMap, pTypeParamMap);
	}
	
	private void printResult(Map<Class<?>, List<Class<?>>> pResultMap) {
		StringBuilder sb = new StringBuilder("\nresult: ");
		for (Map.Entry<Class<?>, List<Class<?>>> mapEntry : pResultMap.entrySet()) {
			sb.append(mapEntry.getKey().getName()).append("<");
			int i = 0;
			for (Class<?> type : mapEntry.getValue()) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(type.getName());
				i++;
			}
			sb.append(">\n");
		}
		if (logger.isTraceEnabled()) {
			logger.trace(sb.toString());
		}
	}
	
	private TypeExtractor() {
	}
}
