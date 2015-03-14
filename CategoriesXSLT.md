### XSLT to transform categories.xml into google-code wiki format ###

```
<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/categories">
    <html>
      <body>
        <xsl:for-each select="*">
            <xsl:text>&#160;* </xsl:text><xsl:value-of select="@name" /> {{{(<xsl:value-of select="@query" />)}}}<br />
            <xsl:for-each select="*">
              <xsl:text>&#160;&#160;* </xsl:text><xsl:value-of select="@name" /> {{{(<xsl:value-of select="@query" />)}}}<br />
              <xsl:for-each select="*">
                <xsl:text>&#160;&#160;&#160;* </xsl:text><xsl:value-of select="@name" /> {{{(<xsl:value-of select="@query" />)}}}<br />
              </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
```

_Only 3 levels are supported, but may be rewriten with recursion_

_<?xml-stylesheet type="text/xsl" href="categories.xsl"?>_