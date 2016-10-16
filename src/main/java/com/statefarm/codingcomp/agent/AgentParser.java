package com.statefarm.codingcomp.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.statefarm.codingcomp.bean.Agent;
import com.statefarm.codingcomp.bean.Product;
import com.statefarm.codingcomp.utilities.SFFileReader;

@Component
public class AgentParser {
	@Autowired
	private SFFileReader sfFileReader;

	@Cacheable(value = "agents")
	public Agent parseAgent(String fileName)
	{
		String file = sfFileReader.readFile(fileName);
		String name =  file.split("<span itemprop=\"name\">")[1].split("</span>")[1];
		ArrayList<String> productNames = new ArrayList<>();
		
		String[] listItems = file.split("regex")[1].split("");
		for (int i = 0;; ++i) {
			productNames.add(listItems[i]);
			if (listItems[i].equals("</div>")) break;
		}
		
		Set<Product> productList  = new TreeSet<>();
		for (String product: productNames) {
			Product p = Product.fromValue(product);
			productList.add(p);
		}
		
		try
		{
			Document doc = Jsoup.connect(fileName).get();
			
			Elements spans = doc.select("span");
			ArrayList<String> postalCodes = new ArrayList<>();
			for (Element e: spans) {
				if (e.attr("itemprop").equals("postalcode"))
					postalCodes.add(e.text());
			}
			
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
}
