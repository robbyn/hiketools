# hiketools

Geographic tools for hikers

#summary One-sentence summary of this page.

## HikeGen

HikeGen takes a GPX file as input, and generates an HTML page with a Google Map and a profile chart.

### Using HikeGen

HikeGen is run with the following command:

```sh
java -jar hikegen.jar [<option>...] <track-file>
```

`<track-file>` contains your hike's track in GPX or XOL (SwissMap) format.

#### Options

<dl>
<dt>`-ele`</dt>
<dd>use the elevation service to update the elevation</dd>
<dt>`-gpx`</dt>
<dd>write a GPX file</dd>
<dt>`-xol`</dt>
<dd>write a XOL file</dd>
<dt>`-sim <`tolerance`>`</dt>
<dd>simplify the track by applying the Douglas-Peucker agorithm, with the given tolerance.
The tolerance is given in meters, the default is 1m. Use `-sim 0` to avoid simplification.</dd>
</dl>
