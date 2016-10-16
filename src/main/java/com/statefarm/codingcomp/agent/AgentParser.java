package com.statefarm.codingcomp.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.statefarm.codingcomp.bean.Address;
import com.statefarm.codingcomp.bean.Agent;
import com.statefarm.codingcomp.bean.Office;
import com.statefarm.codingcomp.bean.Product;
import com.statefarm.codingcomp.bean.USState;
import com.statefarm.codingcomp.utilities.SFFileReader;

@Component
public class AgentParser
{
	@Autowired
	private SFFileReader sfFileReader;

	@Cacheable(value = "agents")
	public Agent parseAgent(String fileName)
	{
		try
		{
			// initiate jsoup
			File file = new File(fileName);
			Document doc = Jsoup.parse(file, "UTF-8", fileName);

			// get the agent's name
			String name = this.getTextFromItemprop(doc, "name").get(0);

			// get the products
			ArrayList<String> productNames = this.getListFromItemprop(doc, "description");
			Set<Product> productList = new TreeSet<Product>();
			
			for(String s: productNames)
			{
				productList.add(Product.fromValue(s));
			}
			
			//get offices
			ArrayList<Office> offices = new ArrayList<>();
			Elements span5List = doc.select("div.span5");
			if (span5List.size() % 2 != 0)
				throw new Exception("the span5 thing is not true");
			for (int i = 0; i < span5List.size(); i += 2) {
				Element firstDiv = span5List.get(i),
						secondDiv = span5List.get(i + 1);
				
				Office office = new Office();
				//phone number
				Elements phoneNumberSpan = secondDiv.select("div div[itemprop=\"telephone\"] span span");
				String phoneNumber = phoneNumberSpan.get(0).text();
				office.setPhoneNumber(phoneNumber);
				
				//language
				Set<String> languages = new TreeSet<>();
				Elements languageList = secondDiv.select("ul"),
						listItems = languageList.select("li");
				
				for (Element e: listItems) {
					Elements languageDiv = e.select("div span div");
					languages.add(languageDiv.get(0).attr("title"));
				}
				
				office.setLanguages(languages);
				
				//office hours
				List<String> officeHours = new ArrayList<>();
				Elements officeHoursList = secondDiv.select("div div span[itemprop=\"openingHours\"]");
				
				for (Element e: officeHoursList)
					officeHours.add(e.text());
				
				office.setOfficeHours(officeHours);
				
				//Address :(
				Address address = new Address();
				//line1 AND line 2
				Elements streetAddressInfo = firstDiv.select("div[itemprop=\"streetAddress\"]");
				
				doc.select("br").append("%%%");
				Element e = streetAddressInfo.get(0);
				
				String[] lines = e.text().split("%%%");
				boolean hasLine2 = lines.length == 2;
				
				address.setLine1(lines[0].trim());
				address.setLine2(!hasLine2 ? null:lines[1]);
				System.out.println(address.getLine2());
				
				//city
				address.setCity(this.getTextFromItemprop(firstDiv, "addressLocality").get(0).trim().substring(0, this.getTextFromItemprop(firstDiv, "addressLocality").get(0).trim().length() - 1));
				//state
				address.setState(USState.fromValue(this.getTextFromItemprop(firstDiv, "addressRegion").get(0)));
				//add the postal code
				address.setPostalCode(this.getTextFromItemprop(firstDiv, "postalCode").get(0).trim());
				office.setAddress(address);
				
				offices.add(office);
			}
			
			Agent a = new Agent();
			a.setName(name);
			a.setProducts(productList);
			a.setOffices(offices);

			return a;
		}

		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private ArrayList<String> getTextFromItemprop(Document doc, String itemprop)
	{
		ArrayList<String> textList = new ArrayList<>();
		Elements spans = doc.select("span");
		for (Element e : spans)
		{
			if (e.attr("itemprop").equals(itemprop))
				textList.add(e.text());
		}

		return textList;
	}
	
	private ArrayList<String> getTextFromItemprop(Element doc, String itemprop)
	{
		ArrayList<String> textList = new ArrayList<>();
		Elements spans = doc.select("span");
		for (Element e : spans)
		{
			if (e.attr("itemprop").equals(itemprop))
				textList.add(e.text());
		}

		return textList;
	}
	
	private ArrayList<String> getListFromItemprop(Document doc, String itemprop) {
		ArrayList<String> textList = new ArrayList<>();
		Elements listItems = doc.select("div[itemprop=\"" + itemprop + "\"] ul li");
		for (Element e: listItems)
			textList.add(e.text());
		
		return textList;
	}
}
