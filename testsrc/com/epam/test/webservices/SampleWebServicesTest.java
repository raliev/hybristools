/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 *
 */
package com.epam.test.webservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.oauth2.constants.OAuth2Constants;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.testsupport.client.WebservicesAssert;
import de.hybris.platform.webservicescommons.testsupport.client.WsRequestBuilder;
import de.hybris.platform.webservicescommons.testsupport.client.WsSecuredRequestBuilder;
import de.hybris.platform.webservicescommons.testsupport.server.NeedsEmbeddedServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import jersey.repackaged.com.google.common.collect.Lists;
import com.epam.constants.HybristoolsserverConstants;
import com.epam.dto.SampleWsDTO;


@NeedsEmbeddedServer(webExtensions =
{ HybristoolsserverConstants.EXTENSIONNAME, OAuth2Constants.EXTENSIONNAME })
@IntegrationTest
public class SampleWebServicesTest extends ServicelayerTest
{
	public static final String OAUTH_CLIENT_ID = "mobile_android";
	public static final String OAUTH_CLIENT_PASS = "secret";

	private static final String BASE_URI = "sample";
	private static final String MAP_URI = BASE_URI + "/map";
	private static final String URI = BASE_URI + "/users";

	private WsRequestBuilder wsRequestBuilder;
	private WsSecuredRequestBuilder wsSecuredRequestBuilder;

	@Before
	public void setUp() throws Exception
	{
		wsRequestBuilder = new WsRequestBuilder()//
				.extensionName(HybristoolsserverConstants.EXTENSIONNAME);

		wsSecuredRequestBuilder = new WsSecuredRequestBuilder()//
				.extensionName(HybristoolsserverConstants.EXTENSIONNAME)//
				.client(OAUTH_CLIENT_ID, OAUTH_CLIENT_PASS)//
				.grantClientCredentials();

		createCoreData();
		createDefaultUsers();
		importCsv("/hybristoolsserver/test/democustomer-data.impex", "utf-8");
	}

	@Test
	public void testGetSampleUsersWithoutAuthorization()
	{
		final Response result = wsRequestBuilder//
				.path(URI)//
				.build()//
				.accept(MediaType.APPLICATION_XML)//
				.get();
		result.bufferEntity();
		WebservicesAssert.assertResponse(Status.UNAUTHORIZED, result);
	}

	@Test
	public void testGetSampleUserUsingClientCredentials()
	{
		final Response result = wsSecuredRequestBuilder//
				.path(URI)//
				.path("user1")//
				.build()//
				.accept(MediaType.APPLICATION_XML)//
				.get();
		result.bufferEntity();
		WebservicesAssert.assertResponse(Status.OK, result);
	}

	@Test
	public void testPostSampleDTO()
	{
		final SampleWsDTO sampleWSDTO = new SampleWsDTO();
		sampleWSDTO.setValue("123");
		final Response result = wsSecuredRequestBuilder//
				.path("sample/dto")//
				.build()//
				.post(Entity.entity(sampleWSDTO, MediaType.APPLICATION_JSON));
		final SampleWsDTO respSampleWSDTO = result.readEntity(SampleWsDTO.class);
		assertNotNull(respSampleWSDTO);
		assertEquals(respSampleWSDTO.getValue(), "123");

	}

	@Test
	public void testPostEmptySampleDTO()
	{
		final SampleWsDTO sampleWSDTO = new SampleWsDTO();
		final Response response = wsSecuredRequestBuilder.path("sample/dto").build()
				.post(Entity.entity(sampleWSDTO, MediaType.APPLICATION_JSON));
		WebservicesAssert.assertResponse(Status.BAD_REQUEST, response);
		final ErrorListWsDTO errors = response.readEntity(ErrorListWsDTO.class);
		assertNotNull(errors);
		assertNotNull(errors.getErrors());
		assertEquals(errors.getErrors().size(), 1);
		final ErrorWsDTO error = errors.getErrors().get(0);
		assertEquals(error.getReason(), "missing");
		assertEquals(error.getSubject(), "value");
		assertEquals(error.getSubjectType(), "parameter");

	}

	@Test
	public void testGetObjectWithMap()
	{
		final Response result = wsSecuredRequestBuilder.path(MAP_URI).build().accept(MediaType.APPLICATION_XML).get();

		WebservicesAssert.assertResponse(Status.OK, result);
		final String entity = result.readEntity(String.class);
		assertNotNull(entity);
		assertTrue(entity.contains("integerKey"));
		assertTrue(entity.contains("10001"));
		assertTrue(entity.contains("StringKey"));
		assertTrue(entity.contains("StringValue"));
	}

	@Test
	public void testPlainString()
	{
		final StringWrapped input = new StringWrapped();
		input.setString("testString");

		final Response result = wsSecuredRequestBuilder.path(BASE_URI).path("plain/string").build()
				.accept(MediaType.APPLICATION_JSON).post(Entity.json(input));
		WebservicesAssert.assertResponse(Status.OK, result);
		final StringWrapped entity = result.readEntity(StringWrapped.class);
		assertNotNull(entity);
		assertEquals(input.string + "1", entity.string);
	}

	@SuppressWarnings("unused")
	private static class StringWrapped
	{
		private String string;

		public void setString(final String string)
		{
			this.string = string;
		}

		public String getString()
		{
			return string;
		}
	}

	@Test
	public void testPlainLong()
	{
		final LongWrapped input = new LongWrapped();
		input.setValue(Long.valueOf(123456789L));

		final Response result = wsSecuredRequestBuilder.path(BASE_URI).path("plain/long").build().accept(MediaType.APPLICATION_JSON)
				.post(Entity.json(input));
		WebservicesAssert.assertResponse(Status.OK, result);
		final LongWrapped entity = result.readEntity(LongWrapped.class);
		assertNotNull(entity);
		assertNotNull(entity.value);
		assertEquals(123456789L + 1L, entity.value.longValue());
	}

	@SuppressWarnings("unused")
	private static class LongWrapped
	{
		private Long value;

		public Long getValue()
		{
			return value;
		}

		public void setValue(final Long value)
		{
			this.value = value;
		}
	}

	@Test
	public void testPlainDouble()
	{
		final DoubleWrapped input = new DoubleWrapped();
		input.setValue(Double.valueOf(12345.6789d));

		final Response result = wsSecuredRequestBuilder.path(BASE_URI).path("plain/double").build()
				.accept(MediaType.APPLICATION_JSON).post(Entity.json(input));
		WebservicesAssert.assertResponse(Status.OK, result);
		final DoubleWrapped entity = result.readEntity(DoubleWrapped.class);
		assertNotNull(entity);
		assertNotNull(entity.value);
		assertEquals(12345.6789d + 1d, entity.value.doubleValue(), 0.0001d);
	}

	@SuppressWarnings("unused")
	private static class DoubleWrapped
	{
		private Double value;

		public Double getValue()
		{
			return value;
		}

		public void setValue(final Double value)
		{
			this.value = value;
		}
	}

	@Test
	public void testGetPlainList()
	{
		final ListWrapper expected = new ListWrapper();
		expected.value = Lists.newArrayList("new String", Double.valueOf(0.123d));

		final Response result = wsSecuredRequestBuilder.path(BASE_URI).path("plain/list").build().accept(MediaType.APPLICATION_JSON)
				.get();

		WebservicesAssert.assertResponse(Status.OK, result);
		final ListWrapper entity = result.readEntity(ListWrapper.class);
		assertNotNull(entity);
		assertEquals(expected.value, entity.value);
	}

	@SuppressWarnings("unused")
	private static class ListWrapper
	{
		private List value;

		public void setValue(final List list)
		{
			this.value = list;
		}

		public List getValue()
		{
			return value;
		}
	}

	@Test
	public void testGetPlainMap()
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("a", "Ala");
		map.put("b", Integer.valueOf(1));
		map.put("c", Lists.newArrayList("a", "b", "c"));

		final Response result = wsSecuredRequestBuilder.path(BASE_URI).path("plain/map").build().accept(MediaType.APPLICATION_JSON)
				.get();

		WebservicesAssert.assertResponse(Status.OK, result);
		final MapWrapped entity = result.readEntity(MapWrapped.class);
		assertNotNull(entity);
		assertNotNull(entity.value);
		assertEquals(map.get("a"), entity.value.get("a"));
		assertEquals(map.get("b"), entity.value.get("b"));
		assertEquals(map.get("c"), entity.value.get("c"));
	}

	@SuppressWarnings("unused")
	private static class MapWrapped
	{
		private Map value;

		public void setValue(final Map value)
		{
			this.value = value;
		}

		public Map getValue()
		{
			return value;
		}
	}

}
