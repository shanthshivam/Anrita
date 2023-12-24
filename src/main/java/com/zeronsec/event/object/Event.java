package com.zeronsec.event.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Event implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 940786714210822482L;

	HashMap<String, Object> singleEvent = new HashMap<>();
	HashMap<String, Object> identicalEvent = new HashMap<>();

	public HashMap<String, Object> getSingleEvent() {
		return singleEvent;
	}

	public void setSingleEvent(String ruleId, String status) {
		//this.singleEvent = singleEvent;
		singleEvent.put(ruleId, status);
	}

	public HashMap<String, Object> getIdenticalEvent() {
		return identicalEvent;
	}

	public void setIdenticalEvent(String ruleId, String status) {
//		this.identicalEvent = identicalEvent;
		identicalEvent.put(ruleId, status);
	}

	
	private String srcIp;
	private String destIp;
	private String destPort;
	private List<Destioc> destioc;
	private List<Srcioc> srcioc;
	private String srcPort;
	private String timestamp;
	private String deviceAction;
	private String destTransIp;
	private String deviceName;
	private String deviceProduct;
	private String deviceVendor;
	private String eventId;
	private String eventDirection;
	private String host;
	private String eventName;
	private String ioc;
	private String ip;
	private String label;
	private String srcInterface;
	private String srcTransIp;
	private String vendorVId;
	private String threat;
	private String msg;
	private String status;
	private String deviceReason;
	private String vendorEName;
	private String destUser;
	private String name;
	private String networkDirection;
	private String deviceHost;
	private String message;
	// Windows Fields
	private String agentHostname;
	private String deviceIp;
	private String destProcessCommand;
	private String destProcessPId;
	private String destProcess;
	private String destFileHashMd5;
	private String destFilePath;
	private String srcProcess;
	private String srcProcessId;
	private String srcFilePath;
	private String vendorEId;
	private String destHashSHA1iocThreatCategory;
	private String destHashMd5iocThreatCategory;
	private String destHashSha256iocThreatCategory;
	private String deviceMac;
	private String deviceOs;
	private String agentName;
	private String logonType;
	private String eventOutcome;
	private String deviceIpv6;
	private String processParentArgs;
	private String processArgs;
	private String processPeOriginalFileName;
	private String processWorkingDirectory;
	private String destFileHashSha1;
	private String hostIp;
	private String winlogIntegrityLevel;
	private String winlogEventDataServiceName;
	private String srcUser;
	private String destDomain;
	private String destinationHostName;
	private String hostMac;
	private String fileDirectory;
	private String destFileName;
	private String winlogEventDataImagePath;
	private String deviceModule;
	private String workstationName;
	private String logonDescription;
	private String sourceNetworkAddress;
	private String accountName;
	private String accountDomain;

	
	public String getWorkstationName() {
		return workstationName;
	}

	public void setWorkstationName(String workstationName) {
		this.workstationName = workstationName;
	}

	public String getLogonDescription() {
		return logonDescription;
	}

	public void setLogonDescription(String logonDescription) {
		this.logonDescription = logonDescription;
	}

	public String getSourceNetworkAddress() {
		return sourceNetworkAddress;
	}

	public void setSourceNetworkAddress(String sourceNetworkAddress) {
		this.sourceNetworkAddress = sourceNetworkAddress;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountDomain() {
		return accountDomain;
	}

	public void setAccountDomain(String accountDomain) {
		this.accountDomain = accountDomain;
	}

	public String getFileDirectory() {
		return fileDirectory;
	}

	public void setFileDirectory(String fileDirectory) {
		this.fileDirectory = fileDirectory;
	}

	public String getDestFileName() {
		return destFileName;
	}

	public void setDestFileName(String destFileName) {
		this.destFileName = destFileName;
	}

	public String getWinlogEventDataImagePath() {
		return winlogEventDataImagePath;
	}

	public void setWinlogEventDataImagePath(String winlogEventDataImagePath) {
		this.winlogEventDataImagePath = winlogEventDataImagePath;
	}

	public String getDeviceModule() {
		return deviceModule;
	}

	public void setDeviceModule(String deviceModule) {
		this.deviceModule = deviceModule;
	}

	public String getHostMac() {
		return hostMac;
	}

	public void setHostMac(String hostMac) {
		this.hostMac = hostMac;
	}

	public String getDestinationHostName() {
		return destinationHostName;
	}

	public void setDestinationHostName(String destinationHostName) {
		this.destinationHostName = destinationHostName;
	}

	public String getSrcUser() {
		return srcUser;
	}

	public void setSrcUser(String srcUser) {
		this.srcUser = srcUser;
	}

	public String getDestDomain() {
		return destDomain;
	}

	public void setDestDomain(String destDomain) {
		this.destDomain = destDomain;
	}

	public String getWinlogEventDataServiceName() {
		return winlogEventDataServiceName;
	}

	public void setWinlogEventDataServiceName(String winlogEventDataServiceName) {
		this.winlogEventDataServiceName = winlogEventDataServiceName;
	}

	public String getProcessParentArgs() {
		return processParentArgs;
	}

	public void setProcessParentArgs(String processParentArgs) {
		this.processParentArgs = processParentArgs;
	}

	public String getProcessArgs() {
		return processArgs;
	}

	public void setProcessArgs(String processArgs) {
		this.processArgs = processArgs;
	}

	public String getProcessPeOriginalFileName() {
		return processPeOriginalFileName;
	}

	public void setProcessPeOriginalFileName(String processPeOriginalFileName) {
		this.processPeOriginalFileName = processPeOriginalFileName;
	}

	public String getProcessWorkingDirectory() {
		return processWorkingDirectory;
	}

	public void setProcessWorkingDirectory(String processWorkingDirectory) {
		this.processWorkingDirectory = processWorkingDirectory;
	}

	public String getDestFileHashSha1() {
		return destFileHashSha1;
	}

	public void setDestFileHashSha1(String destFileHashSha1) {
		this.destFileHashSha1 = destFileHashSha1;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public String getWinlogIntegrityLevel() {
		return winlogIntegrityLevel;
	}

	public void setWinlogIntegrityLevel(String winlogIntegrityLevel) {
		this.winlogIntegrityLevel = winlogIntegrityLevel;
	}

	public String getLogonType() {
		return logonType;
	}

	public void setLogonType(String logonType) {
		this.logonType = logonType;
	}

	public String getEventOutcome() {
		return eventOutcome;
	}

	public void setEventOutcome(String eventOutcome) {
		this.eventOutcome = eventOutcome;
	}

	public String getDeviceIpv6() {
		return deviceIpv6;
	}

	public void setDeviceIpv6(String deviceIpv6) {
		this.deviceIpv6 = deviceIpv6;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getDeviceMac() {
		return deviceMac;
	}

	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	public String getDeviceOs() {
		return deviceOs;
	}

	public void setDeviceOs(String deviceOs) {
		this.deviceOs = deviceOs;
	}

	public String getDestHashSHA1iocThreatCategory() {
		return destHashSHA1iocThreatCategory;
	}

	public void setDestHashSHA1iocThreatCategory(String destHashSHA1iocThreatCategory) {
		this.destHashSHA1iocThreatCategory = destHashSHA1iocThreatCategory;
	}

	public String getDestHashMd5iocThreatCategory() {
		return destHashMd5iocThreatCategory;
	}

	public void setDestHashMd5iocThreatCategory(String destHashMd5iocThreatCategory) {
		this.destHashMd5iocThreatCategory = destHashMd5iocThreatCategory;
	}

	public String getDestHashSha256iocThreatCategory() {
		return destHashSha256iocThreatCategory;
	}

	public void setDestHashSha256iocThreatCategory(String destHashSha256iocThreatCategory) {
		this.destHashSha256iocThreatCategory = destHashSha256iocThreatCategory;
	}

	public String getAgentHostname() {
		return agentHostname;
	}

	public void setAgentHostname(String agentHostname) {
		this.agentHostname = agentHostname;
	}

	public String getDeviceIp() {
		return deviceIp;
	}

	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}

	public String getDestProcessCommand() {
		return destProcessCommand;
	}

	public void setDestProcessCommand(String destProcessCommand) {
		this.destProcessCommand = destProcessCommand;
	}

	public String getDestProcessPId() {
		return destProcessPId;
	}

	public void setDestProcessPId(String destProcessPId) {
		this.destProcessPId = destProcessPId;
	}

	public String getDestProcess() {
		return destProcess;
	}

	public void setDestProcess(String destProcess) {
		this.destProcess = destProcess;
	}

	public String getDestFileHashMd5() {
		return destFileHashMd5;
	}

	public void setDestFileHashMd5(String destFileHashMd5) {
		this.destFileHashMd5 = destFileHashMd5;
	}

	public String getDestFilePath() {
		return destFilePath;
	}

	public void setDestFilePath(String destFilePath) {
		this.destFilePath = destFilePath;
	}

	public String getSrcProcess() {
		return srcProcess;
	}

	public void setSrcProcess(String srcProcess) {
		this.srcProcess = srcProcess;
	}

	public String getSrcProcessId() {
		return srcProcessId;
	}

	public void setSrcProcessId(String srcProcessId) {
		this.srcProcessId = srcProcessId;
	}

	public String getSrcFilePath() {
		return srcFilePath;
	}

	public void setSrcFilePath(String srcFilePath) {
		this.srcFilePath = srcFilePath;
	}

	public String getVendorEId() {
		return vendorEId;
	}

	public void setVendorEId(String vendorEId) {
		this.vendorEId = vendorEId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDeviceHost() {
		return deviceHost;
	}

	public void setDeviceHost(String deviceHost) {
		this.deviceHost = deviceHost;
	}

	public String getNetworkDirection() {
		return networkDirection;
	}

	public void setNetworkDirection(String networkDirection) {
		this.networkDirection = networkDirection;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDestUser() {
		return destUser;
	}

	public void setDestUser(String destUser) {
		this.destUser = destUser;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDeviceReason() {
		return deviceReason;
	}

	public void setDeviceReason(String deviceReason) {
		this.deviceReason = deviceReason;
	}

	public String getVendorEName() {
		return vendorEName;
	}

	public void setVendorEName(String vendorEName) {
		this.vendorEName = vendorEName;
	}

	public String getDestTransIp() {
		return destTransIp;
	}

	public void setDestTransIp(String destTransIp) {
		this.destTransIp = destTransIp;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceProduct() {
		return deviceProduct;
	}

	public void setDeviceProduct(String deviceProduct) {
		this.deviceProduct = deviceProduct;
	}

	public String getDeviceVendor() {
		return deviceVendor;
	}

	public void setDeviceVendor(String deviceVendor) {
		this.deviceVendor = deviceVendor;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventDirection() {
		return eventDirection;
	}

	public void setEventDirection(String eventDirection) {
		this.eventDirection = eventDirection;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getIoc() {
		return ioc;
	}

	public void setIoc(String ioc) {
		this.ioc = ioc;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSrcInterface() {
		return srcInterface;
	}

	public void setSrcInterface(String srcInterface) {
		this.srcInterface = srcInterface;
	}

	public String getSrcTransIp() {
		return srcTransIp;
	}

	public void setSrcTransIp(String srcTransIp) {
		this.srcTransIp = srcTransIp;
	}

	public String getVendorVId() {
		return vendorVId;
	}

	public void setVendorVId(String vendorVId) {
		this.vendorVId = vendorVId;
	}

	public String getThreat() {
		return threat;
	}

	public void setThreat(String threat) {
		this.threat = threat;
	}

	public String getDeviceAction() {
		return deviceAction;
	}

	public void setDeviceAction(String deviceAction) {
		this.deviceAction = deviceAction;
	}

	public String getSrcIp() {
		return srcIp;
	}

	public void setSrcIp(String srcIp) {
		this.srcIp = srcIp;
	}

	public String getDestIp() {
		return destIp;
	}

	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}

	public String getDestPort() {
		return destPort;
	}

	public void setDestPort(String destPort) {
		this.destPort = destPort;
	}

	public List<Destioc> getDestioc() {
		return destioc;
	}

	public void setDestioc(List<Destioc> destioc) {
		this.destioc = destioc;
	}

	public List<Srcioc> getSrcioc() {
		return srcioc;
	}

	public void setSrcioc(List<Srcioc> srcioc) {
		this.srcioc = srcioc;
	}

	public String getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
