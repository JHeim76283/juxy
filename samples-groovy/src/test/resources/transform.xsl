<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output indent="yes" />

    <xsl:template match="book/title">
        <title>
            <xsl:call-template name="tolower" />
        </title>
    </xsl:template>

    <xsl:template match="author">
        <author>
            <firstName>
                <xsl:value-of select="normalize-space(substring-after(., ','))" />
            </firstName>
            <lastName>
                <xsl:value-of select="normalize-space(substring-before(., ','))" />
            </lastName>
        </author>
    </xsl:template>

    <xsl:template name="tolower">
        <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
        <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
        <xsl:value-of select="translate(., $uppercase, $smallcase)" />
    </xsl:template>

    <!-- Default: Recurse. -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select=" @* | node()" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>