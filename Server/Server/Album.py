class Album:
	def __init__(self, user, name, url):
		self.creator = user
		self.name = name
		self.membership = {user.username:url}

	def __repr__(self):
		return '<Album from %r, named %r>' % (self.creator.username, self.name)

	def addUser(self, username, url=None):
		self.membership[username] = url

	def getPermissions(self):
		return self.membership.keys()

	def getPermissionsAndUrl(self):
		return self.membership
