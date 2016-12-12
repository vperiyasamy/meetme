

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
    <a href="getvalue">QueryValue </a>
    </br>
    <a href="getrecommendation">GetMidpoint </a>
    </br>
    <a href="registeruser">RegisterUser </a>
    </br>
    <a href="refreshgroup">RefreshGroup </a>
  </body>
</html>
"""

class ActiveUsers(db.Model):
	user = db.StringProperty()
	active = db.BooleanProperty()


#class StoredData(db.Model):
#	tag = db.StringProperty()
#	value = db.StringProperty(multiline=True)
	## defining value as a string property limits individual values to 500
	## characters.   To remove this limit, define value to be a text
	## property instead, by commnenting out the previous line
	## and replacing it by this one:
	## value db.TextProperty()
	# this line may not be necessary
#	date = db.DateTimeProperty(required=True, auto_now=True)



class SetActive(webapp2.RequestHandler):

	def set_active(self, user, active):
		entry = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user = :1", user).get()
		if entry:
			returnVal(self, lambda : json.dump(["Updated"], self.response.out)) 
		else: 
			entry = ActiveUsers(user = user, active = active)
			returnVal(self, lambda : json.dump(["Stored"], self.response.out)) 
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
	       <p>Active<input type="text" name="active" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Set User Active">
	    </form></body></html>\n''')

class GetValue(webapp2.RequestHandler):

	def get_value(self, tag):
		entry = db.GqlQuery("SELECT * FROM StoredData where tag = :1", tag).get()
		if entry:
			value = entry.value
		else: value = ""
	    # Python supports the creation of anonymous functions (i.e. functions that are 
	    # not bound to a name) at runtime, using a construct called "lambda". 
	    # http://www.secnetix.de/olli/Python/lambda_functions.hawk
	    # json for python:  http://docs.python.org/2/library/json.html
		returnVal(self, lambda : json.dump(["VALUE", tag, value], self.response.out))
    
	    ## The above call to returnVal is equivalent to:
	    #self.response.headers['Content-Type'] = 'application/jsonrequest'
	    #json.dump(["VALUE", tag, value], self.response.out)
    

	def post(self):
		# https://developers.google.com/appengine/docs/python/tools/webapp/requestclass
		tag = self.request.get('tag')
		self.get_value(tag)

	def get(self):
		self.response.out.write('''
	    <html><body>
	    <form action="/getvalue" method="post"
	          enctype=application/x-www-form-urlencoded>
	       <p>Tag<input type="text" name="tag" /></p>
	       <input type="hidden" name="fmt" value="html">
	       <input type="submit" value="Get value">
	    </form></body></html>\n''')
   

class GetRecommendation(webapp2.RequestHandler):

	def get_recommendation(self):
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())
		bucket = '/' + bucket_name


		group = db.GqlQuery("SELECT * FROM ActiveUsers WHERE active != :1", False)

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
		midpoint = get_midpoint(coordinates)

		# choose maximum voted categories by votes
		preferences = sorted(preferences, key=preferences.get, reverse=True)[:10]
		# below is for choosing only the top choice
		#category = max(preferences, key=preferences.get)

		url = 'https://api.foursquare.com/v2/venues/search?v=20161210&ll='
		url += midpoint[0]
		url += ','
		url += midpoint[1]
		url += '&client_id=UGTZZ2JKSHYYCYADYWZ0GRO5C5F0TJOWNTK4JN401AWR444Z&client_secret=EVKPHZ0UXMS1F0PJP5S4UKU4IL0TMHXFWTLEIL3FFBGLNZAF&radius=800&categoryId='

		for category in preferences:
			url += category
			url += ','

		url = url[:-1]

		try:
            #form_data = urllib.urlencode(UrlPostHandler.form_fields)
            #headers = {'Content-Type': 'application/x-www-form-urlencoded'}
            result = urlfetch.fetch( url=url, method=urlfetch.GET, headers = {"Content-Type": "application/json"})

            data = json.loads(result.content)
            name = data['response']['venues'][0]['name']
            lat = data['response']['venues'][0]['location']['lat']
            lon = data['response']['venues'][0]['location']['lng']
            
            returnVal(self, lambda : json.dump([name, lat, lon], self.response.out))

        except urlfetch.Error:
            logging.exception('Caught exception fetching url')


	def get_midpoint(pairs):
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
    

	def post(self):
		# https://developers.google.com/appengine/docs/python/tools/webapp/requestclass
		# pairs = self.request.get('pairs')
		self.get_recommendation(pairs)

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

	def set_available(self, phoneNumber, email, firstName, lastName, lat, lon, categories):

		#update user in db to be active
		entry = db.GqlQuery("SELECT * FROM ActiveUsers WHERE user = :1", phoneNumber).get()
		if entry:
			# do nothing 
		else: 
			entry = ActiveUsers(user = user, active = True)
		entry.put()

		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())
		#self.response.headers['Content-Type'] = 'text/plain'
		#self.response.write('Demo GCS Application running from Version: ' + os.environ['CURRENT_VERSION_ID'] + '\n')
		#self.response.write('Using bucket name: ' + bucket_name + '\n\n')

		bucket = '/' + bucket_name
		filename = bucket + '/' + phoneNumber + '.txt'

		write_retry_params = gcs.RetryParams(backoff_factor=1.1)
		try:
			gcs_file = gcs.open(filename,'w', content_type='text/plain', retry_params=write_retry_params)
			gcs_file.write((phoneNumber + '\n').encode('utf-8'))
			gcs_file.write((email + '\n').encode('utf-8'))
			gcs_file.write((firstName + '\n').encode('utf-8'))
			gcs_file.write((lastName + '\n').encode('utf-8'))
			gcs_file.write((lat + '\n').encode('utf-8'))
			gcs_file.write((lon + '\n').encode('utf-8'))

			for category_vote in categories:
				write_string = category_vote[0] + ' ' + category_vote[1] + '\n'
				gcs_file.write(write_string.encode('utf-8'))

			gcs_file.close()
			returnVal(self, lambda : json.dump(["UserAvailable", tag, value], self.response.out))
		
		except gcs.NotFoundError:
			returnVal(self, lambda : json.dump(["UserNotFound", tag, value], self.response.out))
    

	def post(self):
		# https://developers.google.com/appengine/docs/python/tools/webapp/requestclass
		
		phoneNumber = self.request.get('phone')
		email = self.request.get('email')
		firstName = self.request.get('first')
		lastName = self.request.get('last')
		lat = self.request.get('lat')
		lon = self.request.get('lon')


		cats = []
		categories = []

		# to set an agreed amount of data over the server that is not 84 different strings,
		# the strings will be concatenated on the android side with commas separating values,
		# then they will be sent in 17 packages of agreed length and reprocessed on the
		# server side

		cat1 = self.request.get('cat1')
		cats.append(cat1)
		cat2 = self.request.get('cat2')
		cats.append(cat2)
		cat3 = self.request.get('cat3')
		cats.append(cat3)
		cat4 = self.request.get('cat4')
		cats.append(cat4)
		cat5 = self.request.get('cat5')
		cats.append(cat5)
		cat6 = self.request.get('cat6')
		cats.append(cat6)
		cat7 = self.request.get('cat7')
		cats.append(cat7)
		cat8 = self.request.get('cat8')
		cats.append(cat8)
		cat9 = self.request.get('cat9')
		cats.append(cat9)
		cat10 = self.request.get('cat10')
		cats.append(cat10)
		cat11 = self.request.get('cat11')
		cats.append(cat11)
		cat12 = self.request.get('cat12')
		cats.append(cat12)
		cat13 = self.request.get('cat13')
		cats.append(cat13)
		cat14 = self.request.get('cat14')
		cats.append(cat14)
		cat15 = self.request.get('cat15')
		cats.append(cat15)
		cat16 = self.request.get('cat16')
		cats.append(cat16)
		cat17 = self.request.get('cat17')
		cats.append(cat17)

		for cat in cats:
			pairs = cat.split(';')
			for pair in pairs:
				segs = pair.split(',')
				vote = (segs[0], segs[1])
				categories.append(vote)

		self.set_available(phoneNumber, email, firstName, lastName, lat, lon, categories)

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
	       <p>cat1<input type="text" name="cat1" /></p>
	       <p>cat2<input type="text" name="cat2" /></p>
	       <p>cat3<input type="text" name="cat3" /></p>
	       <p>cat4<input type="text" name="cat4" /></p>
	       <p>cat5<input type="text" name="cat5" /></p>
	       <p>cat6<input type="text" name="cat6" /></p>
	       <p>cat7<input type="text" name="cat7" /></p>
	       <p>cat8<input type="text" name="cat8" /></p>
	       <p>cat9<input type="text" name="cat9" /></p>
	       <p>cat10<input type="text" name="cat10" /></p>
	       <p>cat11<input type="text" name="cat11" /></p>
	       <p>cat12<input type="text" name="cat12" /></p>
	       <p>cat13<input type="text" name="cat13" /></p>
	       <p>cat14<input type="text" name="cat14" /></p>
	       <p>cat15<input type="text" name="cat15" /></p>
	       <p>cat16<input type="text" name="cat16" /></p>
	       <p>cat17<input type="text" name="cat17" /></p>
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

		self.response.write('Creating file %s\n' % filename)

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

class RefreshGroup(webapp2.RequestHandler):

	def refresh_group(self, phoneNumber):
		bucket_name = os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())
		bucket = '/' + bucket_name


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

				gcs_file.close()

			except gcs.NotFoundError:
				returnVal(self, lambda : json.dump(["User not found"], self.response.out))

		returnVal(self, lambda : json.dump(returnlist, self.response.out))
		#if entry:
		#    returnVal(self, lambda : json.dump(["Update"], self.response.out)) 
		#else: 
		#    entry = StoredData(tag = tag, value = value)
		#    returnVal(self, lambda : json.dump(["Store"], self.response.out)) 
		#entry.put()

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
	       <input type="submit" value="Register a User">
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
	('/getvalue', GetValue),
	('/getrecommendation', GetRecommendation),
	('/registeruser', RegisterUser),
	('/refreshgroup', RefreshGroup)
	], debug=True)

