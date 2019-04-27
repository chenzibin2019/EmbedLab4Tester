<?xml version="1.0" encoding="iso-8859-1"?>
<sspaceex xmlns="http://www-verimag.imag.fr/xml-namespaces/sspaceex" version="0.2" math="SpaceEx">
  <component id="bball">
    <param name="x" type="real" local="false" d1="1" d2="1" dynamics="any" />
    <param name="v" type="real" local="false" d1="1" d2="1" dynamics="any" />
    <param name="t" type="real" local="false" d1="1" d2="1" dynamics="any" />
    <param name="g" type="real" local="false" d1="1" d2="1" dynamics="const" />
    <param name="c" type="real" local="false" d1="1" d2="1" dynamics="const" />
    <param name="jump" type="label" local="false" />
    <location id="1" name="falling" x="359.0" y="287.0" width="278.0" height="124.0">
      <invariant>0&lt;=x &amp; x&lt;=10 &amp; -10&lt;=v &amp; v&lt;=10</invariant>
      <flow>t'==1 &amp; x'==v &amp; v' == -g</flow>
    </location>
    <transition source="1" target="1">
      <label>jump</label>
      <guard>x==0 &amp; v&lt;0</guard>
      <assignment>v'==v*(-1)*c &amp; x'==x &amp; t'==t</assignment>
      <labelposition x="-83.0" y="-59.0" width="186.0" height="54.0" />
      <middlepoint x="-2.0" y="-135.0" />
    </transition>
  </component>
  <component id="sys">
    <param name="x" type="real" local="false" d1="1" d2="1" dynamics="any" controlled="true" />
    <param name="v" type="real" local="false" d1="1" d2="1" dynamics="any" controlled="true" />
    <param name="t" type="real" local="false" d1="1" d2="1" dynamics="any" controlled="true" />
    <param name="g" type="real" local="false" d1="1" d2="1" dynamics="const" controlled="true" />
    <param name="c" type="real" local="false" d1="1" d2="1" dynamics="const" controlled="true" />
    <param name="jump" type="label" local="false" />
    <bind component="bball" as="bball_1" x="432.0" y="264.0" width="94.0" height="142.0">
      <map key="x">x</map>
      <map key="v">v</map>
      <map key="t">t</map>
      <map key="g">1</map>
      <map key="c">%s</map>
      <map key="jump">jump</map>
    </bind>
  </component>
</sspaceex>

