package com.classicmodel.frontend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpClientErrorException.NotFound.class)
	public ModelAndView handleNotFound(HttpClientErrorException.NotFound ex) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("errorCode", HttpStatus.NOT_FOUND.value());
		mav.addObject("errorTitle", "Record Not Found");
		mav.addObject("errorMessage",
				"The requested record could not be found. It may have been deleted or the ID is incorrect.");
		return mav;
	}

	@ExceptionHandler(HttpClientErrorException.BadRequest.class)
	public ModelAndView handleBadRequest(HttpClientErrorException.BadRequest ex) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("errorCode", HttpStatus.BAD_REQUEST.value());
		mav.addObject("errorTitle", "Invalid Request");
		mav.addObject("errorMessage", "The request contained invalid data. Please check your input and try again.");
		return mav;
	}

	@ExceptionHandler(HttpClientErrorException.class)
	public ModelAndView handleHttpClientError(HttpClientErrorException ex) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("errorCode", ex.getStatusCode().value());
		mav.addObject("errorTitle", "Request Error");
		mav.addObject("errorMessage",
				"An error occurred while communicating with the backend service. Please try again.");
		return mav;
	}

	@ExceptionHandler(ResourceAccessException.class)
	public ModelAndView handleConnectionError(ResourceAccessException ex) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("errorCode", 503);
		mav.addObject("errorTitle", "Backend Unavailable");
		mav.addObject("errorMessage",
				"Cannot connect to the API backend. Please ensure the backend server is running on port 8080.");
		return mav;
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleGeneral(Exception ex) {
		Throwable cause = (ex instanceof RuntimeException && ex.getCause() != null) ? ex.getCause() : ex;

		if (cause instanceof HttpClientErrorException.NotFound) {
			return handleNotFound((HttpClientErrorException.NotFound) cause);
		}
		if (cause instanceof HttpClientErrorException.BadRequest) {
			return handleBadRequest((HttpClientErrorException.BadRequest) cause);
		}
		if (cause instanceof HttpClientErrorException) {
			return handleHttpClientError((HttpClientErrorException) cause);
		}
		if (cause instanceof ResourceAccessException) {
			return handleConnectionError((ResourceAccessException) cause);
		}

		ModelAndView mav = new ModelAndView("error");
		mav.addObject("errorCode", 500);
		mav.addObject("errorTitle", "Unexpected Error");
		mav.addObject("errorMessage",
				"An unexpected error occurred. Please try again or contact support if the problem persists.");
		return mav;
	}
}
