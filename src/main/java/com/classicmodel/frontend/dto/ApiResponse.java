package com.classicmodel.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

	@JsonProperty("_embedded")
	private EmbeddedWrapper<T> embedded;

	@JsonProperty("page")
	private PageInfo page;

	public EmbeddedWrapper<T> getEmbedded() {
		return embedded;
	}

	public void setEmbedded(EmbeddedWrapper<T> embedded) {
		this.embedded = embedded;
	}

	public PageInfo getPage() {
		return page;
	}

	public void setPage(PageInfo page) {
		this.page = page;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PageInfo {
		private int size;
		private long totalElements;
		private int totalPages;
		private int number;

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public long getTotalElements() {
			return totalElements;
		}

		public void setTotalElements(long totalElements) {
			this.totalElements = totalElements;
		}

		public int getTotalPages() {
			return totalPages;
		}

		public void setTotalPages(int totalPages) {
			this.totalPages = totalPages;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class EmbeddedWrapper<T> {
		private List<T> items;

		public List<T> getItems() {
			return items;
		}

		public void setItems(List<T> items) {
			this.items = items;
		}
	}
}
