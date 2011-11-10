<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
                              xmlns:gmi="http://www.isotc211.org/2005/gmi" 
                              xmlns:gml="http://www.opengis.net/gml" 
                              xmlns:gco="http://www.isotc211.org/2005/gco" 
                              xmlns:gmd="http://www.isotc211.org/2005/gmd">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>
  
  <!-- parameters passed to the XSLT -->
  <xsl:param name="portal_url"/>
  <xsl:param name="identifier_prefix"/>
  
  <!-- constant version of xslt program -->
  <xsl:variable name="version" select='"4.1-20100722-10h20m32s"' />
  
  <!-- strip EUMETSAT Extensions -->
  
  <!-- strip contentInfo -->
  <xsl:template match="gmd:contentInfo"/>
	
  <!-- strip xsi:type from MI_Metadata -->
  <xsl:template match="gmi:MI_Metadata/@xsi:type"/>
  
  <!-- strip schema location-->
  <xsl:template match="@xsi:schemaLocation"/>
 
  <!-- strip gmd:distributionInfo -->
  <xsl:template match="gmd:distributionInfo"/>
  
  <!-- remove acquisitionInformation -->
  <xsl:template match="gmi:acquisitionInformation"/>

  <!-- modify the fileIdentifier to build the wmo identifier -->
  <xsl:template match="gmd:fileIdentifier/gco:CharacterString">
	    <xsl:element name="gco:{local-name()}"><xsl:value-of select="$identifier_prefix"/><xsl:value-of select="normalize-space(.)"/></xsl:element>
  </xsl:template>
  
  <!-- Add content hierarchyLevelName after <gmd:hierarchyLevel> -->
  <xsl:template mode="after" match="gmd:hierarchyLevel"><xsl:element name="gmd:hierarchyLevelName"><xsl:element name="gco:CharacterString">Observation_Sat</xsl:element></xsl:element></xsl:template>
  
  <!-- Add content gmd:distributionInfo before <gmd:identificationInfo> --> 
  <xsl:template mode="after" match="gmd:identificationInfo">
   <gmd:distributionInfo xmlns="http://www.isotc211.org/2005/gmd">
    <gmd:MD_Distribution>
    <gmd:transferOptions>
      <gmd:MD_DigitalTransferOptions>
        <gmd:onLine>
          <gmd:CI_OnlineResource>
            <gmd:linkage>
              <gmd:URL><xsl:value-of select="$portal_url"/>=<xsl:value-of select="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/></gmd:URL>
            </gmd:linkage>
            <gmd:protocol>
              <gco:CharacterString>http</gco:CharacterString>
            </gmd:protocol>
            <gmd:name>
              <gco:CharacterString>EUMETSAT, Product Navigator</gco:CharacterString>
            </gmd:name>
          </gmd:CI_OnlineResource>
        </gmd:onLine>
      </gmd:MD_DigitalTransferOptions>
    </gmd:transferOptions>
  </gmd:MD_Distribution>
 </gmd:distributionInfo>
 </xsl:template>
 
  
  <!-- map MD_Metadata, different namespace and child element order -->
  <xsl:template match="gmi:MI_Metadata">
	<gmd:MD_Metadata xmlns="http://www.isotc211.org/2005/gmd">
		<xsl:attribute name="xsi:schemaLocation">
		   <xsl:value-of select="'http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd'"/>
		</xsl:attribute>
		<xsl:apply-templates select="node()"/>		
	</gmd:MD_Metadata>
  </xsl:template>
  
  <!-- normalize spaces for text portions (this will remove extra spaces CR and so on) -->
  <xsl:template match="text()">
	<xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  
  <!-- template for the attributes -->
  <xsl:template match="@*">
	 <xsl:attribute name="{normalize-space(name())}"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  
  <!-- Boiler Plate + modify template when it start with gmd and clean namespaces -->
  <xsl:template match="*">
    <xsl:apply-templates mode="before" select="."/>
    <xsl:choose>
			<xsl:when test="contains(name(), ':')">
				<xsl:element name="{normalize-space(name())}">
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates mode="add-atts" select="."/>
                    <xsl:apply-templates mode="insert" select="."/>
					<xsl:apply-templates/>
					<xsl:apply-templates mode="append" select="."/>
				</xsl:element>
				<xsl:apply-templates mode="after" select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="gmd:{local-name()}">
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates mode="add-atts" select="."/>
                    <xsl:apply-templates mode="insert" select="."/>
					<xsl:apply-templates/>
					<xsl:apply-templates mode="append" select="."/>
				</xsl:element>
				<xsl:apply-templates mode="after" select="."/>
			</xsl:otherwise>
	</xsl:choose>   
  </xsl:template>

  <!-- the different modes -->
  <xsl:template mode="add-atts" match="*"/>
  <xsl:template mode="insert"   match="*"/>
  <xsl:template mode="append"   match="*"/>
  <xsl:template mode="before"   match="@* | node()"/>
  <xsl:template mode="after"    match="@* | node()"/>

</xsl:stylesheet>
