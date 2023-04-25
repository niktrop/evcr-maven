let Rectangle = \w.\h. {
	width = w,
	height = h,

	area = \this. {
		this.width * this.height
	},

	toString = \this. {
		str(this.width) ++ "x" ++ str(this.height)
	},

	{}
};
Rectangle;;
