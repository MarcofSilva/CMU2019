
class UserAlbums:
	def __init__(self, user, albums=[]):
		self.user = user
		self.albums = albums

	def __repr__(self):
		return '<UserAlbum from %r with %r albums>' % (self.user, len(self.albums))

	def addAlbum(self, album):
		self.albums += [album]

	def getAlbum(self, albumName):
		for a in self.albums:
			print(a.name)
			if a.name == albumName:
				return a
		print("Album not found")