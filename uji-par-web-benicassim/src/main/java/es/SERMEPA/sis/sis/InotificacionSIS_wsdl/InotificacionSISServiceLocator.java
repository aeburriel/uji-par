/**
 * InotificacionSISServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.SERMEPA.sis.sis.InotificacionSIS_wsdl;

import es.uji.apps.par.config.Configuration;

public class InotificacionSISServiceLocator extends org.apache.axis.client.Service 
					implements es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISService {

    public InotificacionSISServiceLocator() {
    }


    public InotificacionSISServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public InotificacionSISServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for InotificacionSIS
    private java.lang.String InotificacionSIS_address = Configuration.getWSDLURL();

    public java.lang.String getInotificacionSISAddress() {
        return InotificacionSIS_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String InotificacionSISWSDDServiceName = "InotificacionSIS";

    public java.lang.String getInotificacionSISWSDDServiceName() {
        return InotificacionSISWSDDServiceName;
    }

    public void setInotificacionSISWSDDServiceName(java.lang.String name) {
        InotificacionSISWSDDServiceName = name;
    }

    public es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISPortType getInotificacionSIS() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(InotificacionSIS_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getInotificacionSIS(endpoint);
    }

    public es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISPortType getInotificacionSIS(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISBindingStub _stub = new es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISBindingStub(portAddress, this);
            _stub.setPortName(getInotificacionSISWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setInotificacionSISEndpointAddress(java.lang.String address) {
        InotificacionSIS_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISBindingStub _stub = new es.SERMEPA.sis.sis.InotificacionSIS_wsdl.InotificacionSISBindingStub(new java.net.URL(InotificacionSIS_address), this);
                _stub.setPortName(getInotificacionSISWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("InotificacionSIS".equals(inputPortName)) {
            return getInotificacionSIS();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("https://sis.SERMEPA.es/sis/InotificacionSIS.wsdl", "InotificacionSISService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("https://sis.SERMEPA.es/sis/InotificacionSIS.wsdl", "InotificacionSIS"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("InotificacionSIS".equals(portName)) {
            setInotificacionSISEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
