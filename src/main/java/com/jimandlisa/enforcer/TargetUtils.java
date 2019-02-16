// Copyright 2019 jimandlisa.com.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package com.jimandlisa.enforcer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

public class TargetUtils {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	static String yaml(File file) throws Exception {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(LINE_SEPARATOR);
			}
		}
		return builder.toString();
	}
	
	static JSONObject json(String yaml) {
		Map<String, Object> map = new Yaml().load(yaml);
		return new JSONObject(map);
	}
	
	@SuppressWarnings("unchecked") // Weak.
	static Map<String, Object> cast(Object obj) {
		return (Map<String, Object>)obj;
	}
	
	static void validate(Map<String, Object> map, Set<String> allowed, String kind) {
		Set<String> set = new HashSet<>(map.keySet());
		set.removeAll(allowed);
		if (!set.isEmpty()) {
			if (set.size() == 1) {
				throw new EnforcerException("Unrecognized " + kind + " key: " + set.iterator().next());
			}
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (String key : CollectionUtils.sort(new ArrayList<>(set))) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(key);
			}
			throw new EnforcerException("Unrecognized " + kind + " keys: " + builder.toString());
		}
	}
	
	static String get(Map<String, Object> map, String key) {
		return (String)map.get(key);
	}
	
	static Integer getInteger(Map<String, Object> map, String key) {
		return (Integer)map.get(key);
	}
	
	@SuppressWarnings("unchecked") // Weak.
	static List<String> getList(Map<String, Object> map, String key) {
		return (List<String>)map.get(key);
	}
	
	private static final Set<String> ALLOWED_LAYER_KEYS = new HashSet<>(Arrays.asList(new String[] {"name", "depth", "description"}));
	private static final Set<String> ALLOWED_DOMAIN_KEYS = new HashSet<>(Arrays.asList(new String[] {"name", "description"}));
	private static final Set<String> ALLOWED_COMPONENT_KEYS = new HashSet<>(Arrays.asList(new String[] {"name", "layer", "domain", "description", "packages"}));
	
	static Layer layer(Map<String, Layer> layers, String name) {
		return layers.get(ArgUtils.check(name, "layer name"));
	}
	
	static Domain domain(Map<String, Domain> domains, String name) {
		return domains.get(ArgUtils.check(name, "domain name"));
	}

	public static Target parse(File file) throws Exception {
		JSONObject json = json(yaml(file));
		Target target = new Target();
		Set<Integer> depths = new HashSet<>();
		for (Object obj : json.getJSONArray("layers").toList()) {
			Map<String, Object> map = cast(obj);
			validate(map, ALLOWED_LAYER_KEYS, "layer");
			Layer layer = new Layer(get(map, "name"), getInteger(map, "depth"), get(map, "description"));
			if (depths.contains(layer.depth())) {
				throw new EnforcerException("Duplicate layer depth " + layer.depth());
			}
			depths.add(layer.depth());
			if (target.layers().containsKey(layer.name())) {
				throw new EnforcerException("Duplicate layer name '" + layer.name() + "'");
			}
			target.add(layer);
		}
		for (Object obj : json.getJSONArray("domains").toList()) {
			Map<String, Object> map = cast(obj);
			validate(map, ALLOWED_DOMAIN_KEYS, "domain");
			Domain domain = new Domain(get(map, "name"), get(map, "description"));
			if (target.domains().containsKey(domain.name())) {
				throw new EnforcerException("Duplicate domain name '" + domain.name() + "'");
			}
			target.add(domain);
		}
		boolean requireDomains = !target.domains().isEmpty();
		for (Object obj : json.getJSONArray("components").toList()) {
			Map<String, Object> map = cast(obj);
			validate(map, ALLOWED_COMPONENT_KEYS, "component");
			Component component = new Component(get(map, "name"), layer(target.layers(), get(map, "layer")), domain(target.domains(), get(map, "domain")), get(map, "description"));
			if (target.components().containsKey(component.name())) {
				throw new EnforcerException("Duplicate component name '" + component.name() + "'");
			}
			if (requireDomains && component.domain() == null) {
				throw new EnforcerException("Must specify domain for component '" + component.name() + "'");
			}
			List<String> packages = getList(map, "packages");
			if (packages != null) {
				component.packages().addAll(CollectionUtils.sort(packages));
			}
			target.add(component);
		}
		return target;
	}
	
	public static void dump(Target target, PrintStream ps) throws Exception {
		List<Layer> layers = new ArrayList<Layer>(target.layers().values());
		Collections.sort(layers, new Comparator<Layer>(){
			@Override
			public int compare(Layer l1, Layer l2) {
				return Integer.valueOf(l1.depth()).compareTo(Integer.valueOf(l2.depth()));
			}});
		List<Domain> domains = new ArrayList<Domain>(target.domains().values());
		Collections.sort(domains, new Comparator<Domain>(){
			@Override
			public int compare(Domain d1, Domain d2) {
				return d1.name().compareTo(d2.name());
			}});
		List<Component> components = new ArrayList<Component>(target.components().values());
		Collections.sort(components, new Comparator<Component>(){
			@Override
			public int compare(Component d1, Component d2) {
				return d1.name().compareTo(d2.name());
			}});
		ps.println("\tLAYERS:");
		for (Layer layer : layers) {
			ps.println("\t\t" + layer);
			ps.println("\t\t" + layer.description());
		}
		ps.println("\tDOMAINS:");
		for (Domain domain : domains) {
			ps.println("\t\t" + domain);
			ps.println("\t\t" + domain.description());
		}
		ps.println("\tCOMPONENTS:");
		for (Component component : components) {
			ps.println("\t\t" + component);
			ps.println("\t\t" + component.description());
			ps.println("\t\tpackages:");
			for (String pkg : component.packages()) {
				ps.println("\t\t\t" + pkg);
			}
		}
	}
}
