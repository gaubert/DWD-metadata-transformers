<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gml="http://www.opengis.net/gml" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:fn="http://www.w3.org/TR/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes"  exclude-result-prefixes="fn xdt">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>
	
	<!-- map MD_Metadata, different namespace and child element order -->
	<xsl:template match="gmi:MI_Metadata">
	<gmd:MD_Metadata xmlns="http://www.isotc211.org/2005/gmd">
		<xsl:attribute name="xsi:schemaLocation"><xsl:value-of select="'http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd'"/></xsl:attribute>
		<gmd:hierarchyLevelName><gco:CharacterString>Observation_Sat</gco:CharacterString></gmd:hierarchyLevelName>
		<xsl:apply-templates select="@*|node()"/>		
	</gmd:MD_Metadata>
	</xsl:template>
	
	<xsl:template match="gmd:contentInfo">
	</xsl:template>
	
	<xsl:template match="gmi:MI_Metadata/@xsi:type"/>
	<xsl:template match="@xsi:schemaLocation">
	</xsl:template>
	<xsl:template match="gmd:distributionInfo">
	</xsl:template>
	<xsl:template match="gmi:acquisitionInformation">
	</xsl:template>
	<xsl:template match="gmd:fileIdentifier/gco:CharacterString">
	    <xsl:element name="gco:{local-name()}">urn:x-wmo:md:int.eumetsat::<xsl:value-of select="normalize-space(.)"/></xsl:element>
	</xsl:template>
	<gmd:hierarchyLevelName>Observation_Sat</gmd:hierarchyLevelName>
	
	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="contains(name(), ':')">
				<xsl:element name="{normalize-space(name())}">
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="gmd:{local-name()}">
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- template for the attributes -->
	<xsl:template match="@*">
		<xsl:attribute name="{normalize-space(name())}"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<!-- template for the text contents -->
	<xsl:template match="text()">
		<xsl:value-of select="normalize-space(.)"/>
	</xsl:template>
</xsl:stylesheet>
