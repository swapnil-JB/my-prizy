package com.sarathi.domain



import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional


@Transactional(readOnly = false)
class PriceController {

	static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
	def priceService
	def preSearchText


	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		respond Price.list(params), model:[priceInstanceCount: Price.count()]
	}

	def search(Integer max) {
		String searchText = params.searchText
		if(searchText != null) {
			preSearchText = searchText
		}
		searchText = searchText==null?"":searchText
		Integer offset = params.offset as Integer
		offset==null?0:offset
		ArrayList<Price> priceInstanceList = priceService.searchByProductBarcode(preSearchText, max, offset);
		def total = priceService.getPriceCountForSearch(preSearchText)
		if(offset.equals(0) && searchText != null) {
			render(view: "index",  model: [priceInstanceList:priceInstanceList, searchText: preSearchText,
				priceInstanceCount:total])
		} else if(offset>0) {
			render(view: "index",  model: [priceInstanceList:priceInstanceList, searchText: preSearchText,
				priceInstanceCount:total])
		} else {
			render(template:'search', model:[priceInstanceList:priceInstanceList, searchText: preSearchText,
				priceInstanceCount:total])
		}
	}

	def show(Price priceInstance) {
		respond priceInstance
	}

	def create() {
		respond new Price(params)
	}

	@Transactional
	def save(Price priceInstance) {
		priceInstance.setProduct(Product.get(priceInstance.getProduct().getBarcode()))
		if (priceInstance == null) {
			notFound()
			return
		}

		if (priceInstance.hasErrors()) {
			respond priceInstance.errors, view:'create'
			return
		}

		priceInstance.save flush:true

		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.created.message', args: [
					message(code: 'price.label',
					default: 'Price'),
					priceInstance.id
				])
				redirect priceInstance
			}
			'*' { respond priceInstance, [status: CREATED] }
		}
	}
	def edit(Price priceInstance) {
		respond priceInstance
	}

	@Transactional
	def update(Price priceInstance) {
		if (priceInstance == null) {
			notFound()
			return
		}

		if (priceInstance.hasErrors()) {
			respond priceInstance.errors, view:'edit'
			return
		}
		priceInstance.save(flush: true, failOnError: true)

		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.updated.message', args: [
					message(code: 'Price.label',
					default: 'Price'),
					priceInstance.id
				])
				redirect priceInstance
			}
			'*'{ respond priceInstance, [status: OK] }
		}
	}

	@Transactional
	def delete(Price priceInstance) {

		if (priceInstance == null) {
			notFound()
			return
		}

		priceInstance.delete()

		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.deleted.message', args: [
					message(code: 'Price.label',
					default: 'Price'),
					priceInstance.id
				])
				redirect action:"index", method:"GET"
			}
			'*'{ render status: NO_CONTENT }
		}
	}

	protected void notFound() {
		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.not.found.message', args: [
					message(code: 'price.label',
					default: 'Price'),
					params.id
				])
				redirect action: "index", method: "GET"
			}
			'*'{ render status: NOT_FOUND }
		}
	}
}
