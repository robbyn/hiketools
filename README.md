# WARNING

This project is not maintained anymore: it is superceded by project [Gianadda](http://gianadda.perry.productions/)

# HikeTools

Geographic tools for hikers

HikeTools takes a GPX file as input (or a XOL), and generates an HTML page with a Google Map and a profile chart.

## Using HikeTools

HikeTools is run with the following command:

```sh
java -jar hiketools.jar [<option>...] <track-file>
```

`<track-file>` contains your hike's track in GPX or XOL (SwissMap) format.

### Options

<dl>
<dt>-ele</dt>
<dd>use the elevation service to update the elevation</dd>
<dt>-gpx</dt>
<dd>write a GPX file</dd>
<dt>-xol</dt>
<dd>write a XOL file</dd>
<dt>-sim &lt;tolerance&gt;</dt>
<dd>simplify the track by applying the Douglas-Peucker agorithm, with the given tolerance.
The tolerance is given in meters, the default is 1m. Use `-sim 0` to avoid simplification altogether.</dd>
</dl>
