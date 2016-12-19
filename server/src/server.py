

from google.appengine.ext import db
import webapp2
import json # without this, you will have error 500
import math

import logging
import os
import cloudstorage as gcs

import urllib
import urllib2

from google.appengine.api import urlfetch
from google.appengine.api import app_identity

MAIN_PAGE_HTML = """\
<html>
  <body>
    <a href="setactive">SetActive </a>
    </br>
    <a href="getrecommendation">GetRecommendation </a>
    </br>
    <a href="setavailable">SetAvailable </a>
    </br>
    <a href="registeruser">RegisterUser </a>
    </br>
    <a href="unregisteruser">UnregisterUser </a>
    </br>
    <a href="refreshgroup">RefreshGroup </a>
  </body>
</html>
"""

class ActiveUsers(db.Model):
	user = db.StringProperty()
	active = db.BooleanProperty()

class PreviousRecs(db.Model):
	place = db.StringProperty()
	visited = db.BooleanProperty()



class SetActive(webapp2.RequestHandler):

	def set_active(self, user, active):
		entry = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user = :1", user).get()
		if entry:
			returnVal(self, lambda : json.dump(["Updated"], self.response.out)) 
		else:
			returnVal(self, lambda : json.dump(["Stored"], self.response.out))
		entry = ActiveUsers(key_name = user, user = user, active = active)
		entry.put()

	def post(self):
		user = self.request.get('user')
		if self.request.get('value') == 'active':
			active = True
		else:
			active = False
		self.set_active(user, active)

# this is just for browser test
	def get(self):
		self.response.out.write('''
		<html><body>
		<form action="/setactive" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <p>User<input type="text" name="user" /></p>
	       <p>Value<input type="text" name="value" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Set User Active">
	    </form></body></html>\n''')
   

class GetRecommendation(webapp2.RequestHandler):

	def get_midpoint(self, pairs):
		cartesian = []
		w = 0

		# convert latitude and longitude to cartesian coordinates
		for lat, lon in pairs:
			# convert to radians
			lat = lat * (math.pi) / 180 
			lon = lon * (math.pi) / 180

			# cartesian conversion
			x = math.cos(lat) * math.cos(lon)
			y = math.cos(lat) * math.sin(lon)
			z = math.sin(lat)

			# add to list of coordinates
			xyz = (x, y, z)
			cartesian.append(xyz)

			w += 1  # w represents the weight of each location, we will take it to be 1 equally for all

		# these will be the final cartesian coordinates of the midpoint
		x = 0
		y = 0
		z = 0

		#next, compute weighted average of each coordinate
		for xyz in cartesian:
			x += xyz[0]
			y += xyz[1]
			z += xyz[2]

		x /= w
		y /= w
		z /= w

		#lastly, convert back to latitude and longitude
		lon = math.atan2(y, x)
		hyp = math.sqrt( (x * x) + (y * y) )
		lat = math.atan2(z, hyp)

		lat = lat * 180 / math.pi
		lon = lon * 180 / math.pi

		return (lat, lon)

	def get_recommendation(self):
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())
		bucket = '/' + bucket_name


		group = db.GqlQuery("SELECT * FROM ActiveUsers WHERE active = :1", True)

		if group.count(1) == 0:
			returnVal(self, lambda : json.dump(["No Active Users"], self.response.out))
			return

		# each of the following tags corresponds to a restaurant category on the Foursquare API for searching locations
		# each restaurant category initialized to 0 for the tallying of preferences
		preferences = {
		'503288ae91d4c4b30a586d67': 0, '4bf58dd8d48988d1c8941735': 0, '4bf58dd8d48988d10a941735': 0, '4bf58dd8d48988d14e941735': 0, '4bf58dd8d48988d142941735': 0,
		'56aa371be4b08b9a8d573568': 0, '52e81612bcbc57f1066b7a03': 0, '4bf58dd8d48988d145941735': 0, '52af3a7c3cf9994f4e043bed': 0, '4bf58dd8d48988d1f5931735': 0,
		'52af3aaa3cf9994f4e043bf0': 0, '52af3afc3cf9994f4e043bf8': 0, '52af3b463cf9994f4e043bfe': 0, '52af3b593cf9994f4e043c00': 0, '52af3b773cf9994f4e043c03': 0,
		'52af3b813cf9994f4e043c04': 0, '4eb1bd1c3b7b55596b4a748f': 0, '52e81612bcbc57f1066b79fb': 0, '52af0bd33cf9994f4e043bdd': 0, '4deefc054765f83613cdba6f': 0,
		'4bf58dd8d48988d111941735': 0, '55a59bace4b013909087cb24': 0, '55a59bace4b013909087cb27': 0, '4bf58dd8d48988d1d2941735': 0, '55a59bace4b013909087cb2a': 0,
		'4bf58dd8d48988d113941735': 0, '4bf58dd8d48988d156941735': 0, '4eb1d5724b900d56c88a45fe': 0, '4bf58dd8d48988d1d1941735': 0, '4bf58dd8d48988d149941735': 0,
		'52af39fb3cf9994f4e043be9': 0, '4bf58dd8d48988d14a941735': 0, '4bf58dd8d48988d169941735': 0, '4bf58dd8d48988d1df931735': 0, '4bf58dd8d48988d16a941735': 0,
		'4bf58dd8d48988d143941735': 0, '52e81612bcbc57f1066b7a0c': 0, '52e81612bcbc57f1066b79f4': 0, '4bf58dd8d48988d16c941735': 0, '4bf58dd8d48988d17a941735': 0,
		'4bf58dd8d48988d144941735': 0, '4bf58dd8d48988d1e0931735': 0, '52e81612bcbc57f1066b79f2': 0, '4bf58dd8d48988d1d0941735': 0, '512e7cae91d4cbb4e5efe0af': 0,
		'4bf58dd8d48988d1c9941735': 0, '4bf58dd8d48988d147941735': 0, '4bf58dd8d48988d148941735': 0, '52e81612bcbc57f1066b7a05': 0, '4bf58dd8d48988d10b941735': 0,
		'4bf58dd8d48988d16e941735': 0, '4bf58dd8d48988d1cb941735': 0, '4bf58dd8d48988d10c941735': 0, '4d4ae6fc7a7b7dea34424761': 0, '4bf58dd8d48988d10d941735': 0,
		'4bf58dd8d48988d10e941735': 0, '4bf58dd8d48988d16f941735': 0, '4bf58dd8d48988d10f941735': 0, '54135bf5e4b08f3d2429dfdd': 0, '54135bf5e4b08f3d2429dfde': 0,
		'52e81612bcbc57f1066b7a06': 0, '4bf58dd8d48988d110941735': 0, '4bf58dd8d48988d1be941735': 0, '4bf58dd8d48988d1bf941735': 0, '4bf58dd8d48988d1c0941735': 0,
		'4bf58dd8d48988d1c1941735': 0, '4bf58dd8d48988d153941735': 0, '4bf58dd8d48988d151941735': 0, '56aa371ae4b08b9a8d5734ba': 0, '4bf58dd8d48988d115941735': 0,
		'4bf58dd8d48988d1ca941735': 0, '4def73e84765ae376e57713a': 0, '56aa371be4b08b9a8d5734c7': 0, '4bf58dd8d48988d1c5941735': 0, '4bf58dd8d48988d1ce941735': 0,
		'4bf58dd8d48988d14f941735': 0, '4bf58dd8d48988d150941735': 0, '5413605de4b0ae91d18581a9': 0, '4bf58dd8d48988d1cc941735': 0, '4bf58dd8d48988d1d3941735': 0,
		'4bf58dd8d48988d14c941735': 0 }

		coordinates = []

		for person in group:
			filename = bucket + '/' + person.user + '.txt'
			try:
				gcs_file = gcs.open(filename,'r')
				
				# skip phone number, email, first name, and last name
				gcs_file.readline()
				gcs_file.readline()
				gcs_file.readline()
				gcs_file.readline()

				lat = float(gcs_file.readline()[:-1])
				lon = float(gcs_file.readline()[:-1])
				
				#capture latitude and longitude in a pair and append to list
				coords = (lat, lon)
				coordinates.append(coords)

				line = gcs_file.readline()[:-1]

				# loop through user's file for all preferences and their votes on them
				while line != '':
					segments = line.split()
					category = segments[0]
					vote = int(segments[1])
					preferences[category] += vote
					line = gcs_file.readline()[:-1]

				gcs_file.close()

			except gcs.NotFoundError:
				returnVal(self, lambda : json.dump(["User not found"], self.response.out))

		# call midpoint function to find geographic midpoint
		midpoint = self.get_midpoint(coordinates)

		# choose maximum voted categories by votes
		preferences = sorted(preferences, key=preferences.get, reverse=True)[:10]
		# below is for choosing only the top choice
		#category = max(preferences, key=preferences.get)

		url = 'https://api.foursquare.com/v2/venues/search?v=20161210&ll='
		url += str(midpoint[0])
		url += ','
		url += str(midpoint[1])
		url += '&client_id=UGTZZ2JKSHYYCYADYWZ0GRO5C5F0TJOWNTK4JN401AWR444Z&client_secret=EVKPHZ0UXMS1F0PJP5S4UKU4IL0TMHXFWTLEIL3FFBGLNZAF&radius=800&categoryId='

		for category in preferences:
			url += category
			url += ','

		url = url[:-1]

		try:
			result = urlfetch.fetch( url=url, method=urlfetch.GET, headers = {"Content-Type": "application/json"})
			
			data = json.loads(result.content)

			# initializing loop variables to go through json dump
			# looking for a restaurant not recently visited
			found = False
			atleast_one = False
			visited = False
			name = '' # name of place to return to application
			phone = '' # phone number of place to return
			lat = '' # latitude of place
			lon = '' # longitude of place
			i = 0 # loop variable

			try:
				while not found:
					entry = db.GqlQuery("SELECT * FROM PreviousRecs where place = :1", data['response']['venues'][i]['name']).get()
					# already have restaurant in db
					if entry:
						atleast_one = True # we know we've found atleast one place for users
						if entry.visited:
							# since recently visited, don't select this
							visited = False # clear bool for next time it pops up
							name = data['response']['venues'][i]['name']
						else :
							# since not recently visited, select this
							found = True # exit loop now 
							visited = True # mark recently visited
							name = data['response']['venues'][i]['name']
							phone = data['response']['venues'][i]['contact']['phone']
							lat = data['response']['venues'][i]['location']['lat']
							lon = data['response']['venues'][i]['location']['lng']
					else:
						# this is a new place! select this
						found = True # exit loop now
						visited = True # mark recently visited
						name = data['response']['venues'][i]['name']
						phone = data['response']['venues'][i]['contact']['phone']
						lat = data['response']['venues'][i]['location']['lat']
						lon = data['response']['venues'][i]['location']['lng']

					# add (or update) place to db
					db_entry = PreviousRecs(key_name = name, place = name, visited = visited)	
					db_entry.put()		
					i += 1 # go to next restaurant (if not exitting)

			# no new or not recently visited restaurants were found in entire json dump
			# however, if we found one already visited, go there
			except ValueError:
				if atleast_one:
					found = False
					i = 0 # restart search from beginning of json
					while not found:
						entry = db.GqlQuery("SELECT * FROM PreviousRecs where place = :1", data['response']['venues'][i]['name']).get()
						if entry:
							found = True # exit loop now
							# don't need to update db, because it's already marked recently visited
							name = data['response']['venues'][i]['name']
							phone = data['response']['venues'][i]['contact']['phone']
							lat = data['response']['venues'][i]['location']['lat']
							lon = data['response']['venues'][i]['location']['lng']
						else:
							continue # we know there is atleast one restaurant so just keep looking
						i += 1

				else:
					# if we get here, absolutely no restaurants match the search criteria within 1000m
					returnVal(self, lambda : json.dump(["none"], self.response.out))

			returnVal(self, lambda : json.dump([name, phone, lat, lon], self.response.out))
			# set recommendation flag for group
			filename = bucket + '/recommendation.txt'
			rec_file = gcs.open(filename, 'w', content_type='text/plain')
			rec_file.write(('yes').encode('utf-8'))
			rec_file.close()

		except urlfetch.Error:
			logging.exception('Caught exception fetching url')


	def post(self):
		# https://developers.google.com/appengine/docs/python/tools/webapp/requestclass
		# pairs = self.request.get('pairs')
		self.get_recommendation()

	def get(self):
		self.response.out.write('''
	    <html>
	    <body>
	    <form action="/getrecommendation" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Get Recommendation">
	    </form>
	    </body>
	    </html>\n''') 


class SetAvailable(webapp2.RequestHandler):

	def set_available(self, user, email, firstName, lastName, lat, lon, categories):

		#update user in db to be active
		entry = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user = :1", user).get()
		if entry:
			entry.active = True
			entry.put()
		else:
			returnVal(self, lambda : json.dump(["UserNotFound"], self.response.out))

		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())

		bucket = '/' + bucket_name

		# clear recommendation from group if new user is available
		filename = bucket + '/recommendation.txt'
		rec_file = gcs.open(filename, 'w', content_type='text/plain')
		rec_file.write(('no').encode('utf-8'))
		rec_file.close()

		filename = bucket + '/' + user + '.txt'

		write_retry_params = gcs.RetryParams(backoff_factor=1.1)
		try:
			gcs_file = gcs.open(filename,'w', content_type='text/plain', retry_params=write_retry_params)
			gcs_file.write((user + '\n').encode('utf-8'))
			gcs_file.write((email + '\n').encode('utf-8'))
			gcs_file.write((firstName + '\n').encode('utf-8'))
			gcs_file.write((lastName + '\n').encode('utf-8'))
			gcs_file.write((lat + '\n').encode('utf-8'))
			gcs_file.write((lon + '\n').encode('utf-8'))

			for category_vote in categories:
				write_string = category_vote[0] + ' ' + category_vote[1] + '\n'
				gcs_file.write(write_string.encode('utf-8'))

			gcs_file.close()
			returnVal(self, lambda : json.dump(["UserAvailable"], self.response.out))
		
		except gcs.NotFoundError:
			returnVal(self, lambda : json.dump(["UserNotFound"], self.response.out))


	def post(self):
		# https://developers.google.com/appengine/docs/python/tools/webapp/requestclass
		
		user = self.request.get('phone')
		email = self.request.get('email')
		firstName = self.request.get('first')
		lastName = self.request.get('last')
		lat = self.request.get('lat')
		lon = self.request.get('lon')

		# categories and their votes will all be sent as one string delimited
		# by the ';' character, with a category and its vote delimited by the
		# ',' character.

		cats = self.request.get('cats')

		categories = []

		pairs = cats.split(';')
		for pair in pairs:
			segs = pair.split(',')
			vote = (segs[0], segs[1])
			categories.append(vote)

		self.set_available(user, email, firstName, lastName, lat, lon, categories)

	def get(self):
		self.response.out.write('''
	    <html><body>
	    <form action="/setavailable" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <p>Phone<input type="text" name="phone" /></p>
	       <p>Email<input type="text" name="email" /></p>
	       <p>First<input type="text" name="first" /></p>
	       <p>Last<input type="text" name="last" /></p>
	       <p>Lat<input type="text" name="lat" /></p>
	       <p>Lon<input type="text" name="lon" /></p>
	       <p>categories<input type="text" name="cats" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Set Available">
	    </form></body></html>\n''')

class RegisterUser(webapp2.RequestHandler):

	def register_user(self, phoneNumber, email, firstName, lastName):
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())
		#self.response.headers['Content-Type'] = 'text/plain'
		#self.response.write('Demo GCS Application running from Version: ' + os.environ['CURRENT_VERSION_ID'] + '\n')
		#self.response.write('Using bucket name: ' + bucket_name + '\n\n')

		bucket = '/' + bucket_name
		filename = bucket + '/' + phoneNumber + '.txt'
		#Create a file.

		#The retry_params specified in the open call will override the default
		#retry params for this particular file handle.

		#self.response.write('Creating file %s\n' % filename)

		write_retry_params = gcs.RetryParams(backoff_factor=1.1)
		try:
			gcs_file = gcs.open(filename,'r')
			gcs_file.close()
			returnVal(self, lambda :json.dump(["AlreadyRegistered"], self.response.out))

		except gcs.NotFoundError:
			gcs_file = gcs.open(filename,'w', content_type='text/plain', retry_params=write_retry_params)
			gcs_file.write((phoneNumber + '\n').encode('utf-8'))
			gcs_file.write((email + '\n').encode('utf-8'))
			gcs_file.write((firstName + '\n').encode('utf-8'))
			gcs_file.write((lastName + '\n').encode('utf-8'))
			gcs_file.close()

			# enter them in db
			entry = ActiveUsers(key_name = phoneNumber, user = phoneNumber, active = False)
			entry.put()

			returnVal(self, lambda : json.dump(["Success"], self.response.out))

		#entry = db.GqlQuery("SELECT * FROM StoredData where tag = :1", tag).get()
		#if entry:
		#    returnVal(self, lambda : json.dump(["Update"], self.response.out)) 
		#else: 
		#    entry = StoredData(tag = tag, value = value)
		#    returnVal(self, lambda : json.dump(["Store"], self.response.out)) 
		#entry.put()

	def post(self):
		phoneNumber = self.request.get('phone')
		email = self.request.get('email')
		firstName = self.request.get('first')
		lastName = self.request.get('last')
		self.register_user(phoneNumber, email, firstName, lastName)

# this is just for browser test
	def get(self):
		self.response.out.write('''
	    <html><body>
	    <form action="/registeruser" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <p>Phone Number<input type="text" name="phone" /></p>
	       <p>Email<input type="text" name="email" /></p>
	       <p>First Name<input type="text" name="first" /></p>
	       <p>Last Name<input type="text" name="last" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Register a User">
	    </form></body></html>\n''')

class UnregisterUser(webapp2.RequestHandler):

	def unregister_user(self, phoneNumber):

		# first remove them from db
		query = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user = :1", phoneNumber)
		if query:
			db_success = True
			user = query.fetch(10)
			db.delete(user)
		else:
			db_success = False

		# next remove them from file system
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())

		bucket = '/' + bucket_name
		filename = bucket + '/' + phoneNumber + '.txt'

		try:
			gcs.delete(filename)
			if db_success:
				returnVal(self, lambda :json.dump(["Success"], self.response.out))
			else:
				returnVal(self, lambda : json.dump(["Error - No user with that phone number"], self.response.out))

		except gcs.NotFoundError:
			returnVal(self, lambda : json.dump(["Error - No userfile with that phone number"], self.response.out))

	def post(self):
		phoneNumber = self.request.get('phone')
		self.unregister_user(phoneNumber)

# this is just for browser test
	def get(self):
		self.response.out.write('''
	    <html><body>
	    <form action="/unregisteruser" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <p>Phone Number<input type="text" name="phone" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Unregister a User">
	    </form></body></html>\n''')

class RefreshGroup(webapp2.RequestHandler):

	def refresh_group(self, phoneNumber):
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())
		bucket = '/' + bucket_name

		send_rec = "False"
		user = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user = :1", phoneNumber).get()
		if user.active:
			filename = bucket + '/recommendation.txt'
			rec_file = gcs.open(filename, 'r')
			yes_or_no = rec_file.readline()
			if yes_or_no.lower() == 'yes':
				send_rec = "True"
			rec_file.close()


		group = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user != :1", phoneNumber)

		returnlist = []
		for person in group:
			filename = bucket + '/' + person.user + '.txt'
			try:
				gcs_file = gcs.open(filename,'r')
				
				# skip phone number and email
				gcs_file.readline()
				gcs_file.readline()

				returnlist.append(person.user)
				returnlist.append(gcs_file.readline()[:-1])
				returnlist.append(gcs_file.readline()[:-1])
				returnlist.append(person.active)
				if person.active:
					returnlist.append(gcs_file.readline()[:-1]) # latitude
					returnlist.append(gcs_file.readline()[:-1]) # longitude

				gcs_file.close()

			except gcs.NotFoundError:
				returnVal(self, lambda : json.dump(["User not found"], self.response.out))

		returnlist.insert(0, send_rec)

		returnVal(self, lambda : json.dump(returnlist, self.response.out))


	def post(self):
		phoneNumber = self.request.get('phone')
		self.refresh_group(phoneNumber)

# this is just for browser test
	def get(self):
		self.response.out.write('''
	    <html><body>
	    <form action="/refreshgroup" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <p>Phone Number<input type="text" name="phone" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Refresh Group">
	    </form></body></html>\n''')


class DeleteAll(webapp2.RequestHandler):

	def delete_all(self):

		# first remove from file system
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())

		bucket = '/' + bucket_name

		# first remove them from db
		group = db.GqlQuery("SELECT * FROM ActiveUsers")
		
		for person in group:
			filename = bucket + '/' + person.user + '.txt'

			try:
				gcs.delete(filename)
			except gcs.NotFoundError:
				returnVal(self, lambda : json.dump(["Error - No userfile with that phone number"], self.response.out))

		group = db.GqlQuery("SELECT * FROM ActiveUsers")
		users = group.fetch(100)
		db.delete(users)

		returnVal(self, lambda : json.dump(["All Users Deleted"], self.response.out))


	def post(self):
		self.delete_all()

# this is just for browser test
	def get(self):
		self.response.out.write('''
	    <html><body>
	    <form action="/deleteall" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="DELETE EVERYONE">
	    </form></body></html>\n''')

#### Utilty procedures for generating the output
#### Handler is an appengine request handler.  writer is a thunk
#### (i.e. a procedure of no arguments) that does the write when invoked.
def returnVal(handler, writer):
	handler.response.headers['Content-Type'] = 'application/jsonrequest'
	writer()


class MainPage(webapp2.RequestHandler):
	def get(self):
		# https://developers.google.com/appengine/docs/python/tools/webapp/responseclass
		self.response.write(MAIN_PAGE_HTML)

# webapp2 overview:   http://webapp-improved.appspot.com/index.html
# Webapp2 WSGI application:
# http://webapp-improved.appspot.com/guide/app.html
application = webapp2.WSGIApplication([
	('/', MainPage),
	('/setactive', SetActive),
	('/getrecommendation', GetRecommendation),
	('/setavailable', SetAvailable),
	('/registeruser', RegisterUser),
	('/unregisteruser', UnregisterUser),
	('/refreshgroup', RefreshGroup),
	('/deleteall', DeleteAll)
	], debug=True)

